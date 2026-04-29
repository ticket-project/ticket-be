#!/usr/bin/env bash
# PR 리뷰 상태 확인 + 대기 모드 지원
#
# 사용법:
#   ./scripts/review-respond.sh                    # 현재 상태만 확인
#   ./scripts/review-respond.sh --wait             # 리뷰 도착할 때까지 대기 후 확인
#   ./scripts/review-respond.sh --wait --auto-merge # 대기 후 P0 없으면 자동 병합
#   ./scripts/review-respond.sh --auto-merge 42    # PR #42 자동 병합
#
# 종료 코드:
#   0 = 병합 완료 또는 병합 가능
#   1 = 에러
#   2 = 변경 요청됨 (수정 필요)
#   3 = 아직 대기 중 (리뷰 미완료)

set -euo pipefail

WAIT_MODE=false
AUTO_MERGE=false
PR_NUMBER=""

# 인자 파싱
for arg in "$@"; do
    case "$arg" in
        --wait) WAIT_MODE=true ;;
        --auto-merge) AUTO_MERGE=true ;;
        *)
            if [[ "$arg" =~ ^[0-9]+$ ]]; then
                PR_NUMBER="$arg"
            fi
            ;;
    esac
done

# PR 번호 자동 감지
if [ -z "$PR_NUMBER" ]; then
    PR_NUMBER=$(gh pr view --json number -q '.number' 2>/dev/null || echo "")
    if [ -z "$PR_NUMBER" ]; then
        echo "[ERROR] 현재 브랜치에 연결된 PR이 없습니다"
        echo "[INFO] 사용법: ./scripts/review-respond.sh [--wait] [PR번호]"
        exit 1
    fi
fi

# 알려진 리뷰 봇 목록
KNOWN_BOTS="coderabbitai|github-actions|copilot|codescene-ci-cd|codex|cr-bot|sweep-ai"

# ──────────────────────────────────────────────
# 리뷰 대기 모드
# ──────────────────────────────────────────────
if [ "$WAIT_MODE" = true ]; then
    MAX_WAIT=${MAX_WAIT:-600}      # 최대 대기 시간 (기본 10분)
    POLL_INTERVAL=${POLL_INTERVAL:-30}  # 폴링 간격 (기본 30초)
    ELAPSED=0

    echo "[INFO] PR #$PR_NUMBER 리뷰 대기 모드 (최대 ${MAX_WAIT}초, ${POLL_INTERVAL}초 간격)"
    echo ""

    while [ "$ELAPSED" -lt "$MAX_WAIT" ]; do
        # CI 체크 상태 확인
        CHECKS_OUTPUT=$(gh pr checks "$PR_NUMBER" 2>/dev/null || echo "")
        PENDING_CHECKS=$(echo "$CHECKS_OUTPUT" | grep -c "pending\|queued\|in_progress\|waiting" || echo "0")
        FAILED_CHECKS=$(echo "$CHECKS_OUTPUT" | grep -c "fail" || echo "0")

        # 리뷰 코멘트 수 확인
        REVIEW_COUNT=$(gh api "repos/{owner}/{repo}/pulls/$PR_NUMBER/reviews" \
            --jq 'length' 2>/dev/null || echo "0")
        COMMENT_COUNT=$(gh api "repos/{owner}/{repo}/issues/$PR_NUMBER/comments" \
            --jq 'length' 2>/dev/null || echo "0")

        # 봇 코멘트 수 (리뷰 도구가 응답했는지)
        BOT_COMMENT_COUNT=$(gh api "repos/{owner}/{repo}/issues/$PR_NUMBER/comments" \
            --jq "[.[] | select(.user.login | test(\"$KNOWN_BOTS\"; \"i\"))] | length" \
            2>/dev/null || echo "0")

        # CI 실패 → 즉시 중단
        if [ "$FAILED_CHECKS" -gt 0 ]; then
            echo "[DONE] CI 체크 실패 감지 (${ELAPSED}초 경과)"
            echo ""
            break
        fi

        # 완료 조건: CI 체크 모두 완료 + 봇 코멘트 1개 이상
        if [ "$PENDING_CHECKS" -eq 0 ] && [ "$BOT_COMMENT_COUNT" -gt 0 ]; then
            echo "[DONE] 리뷰 도착 (${ELAPSED}초 경과, 봇 코멘트 ${BOT_COMMENT_COUNT}개)"
            echo ""
            break
        fi

        # 상태 출력
        echo "  [${ELAPSED}s] CI 대기: ${PENDING_CHECKS} | 리뷰: ${REVIEW_COUNT} | 봇 코멘트: ${BOT_COMMENT_COUNT}"

        sleep "$POLL_INTERVAL"
        ELAPSED=$((ELAPSED + POLL_INTERVAL))
    done

    if [ "$ELAPSED" -ge "$MAX_WAIT" ]; then
        echo "[WARN] 최대 대기 시간 초과 (${MAX_WAIT}초). 현재 상태를 출력합니다."
        echo ""
    fi
fi

# ──────────────────────────────────────────────
# 리뷰 상태 출력
# ──────────────────────────────────────────────
echo "=== PR #$PR_NUMBER 리뷰 상태 ==="
echo ""

# 1. PR 기본 정보
echo "[PR 정보]"
gh pr view "$PR_NUMBER" --json title,state,mergeable,reviewDecision \
    --template '  제목: {{.title}}
  상태: {{.state}}
  병합 가능: {{.mergeable}}
  리뷰 결정: {{.reviewDecision}}
'
echo ""

# 2. CI 체크 상태
echo "[CI 체크]"
CHECKS_OUTPUT=$(gh pr checks "$PR_NUMBER" 2>/dev/null || echo "")
if [ -n "$CHECKS_OUTPUT" ]; then
    echo "$CHECKS_OUTPUT"
    PENDING_CHECKS=$(echo "$CHECKS_OUTPUT" | grep -c "pending\|queued\|in_progress\|waiting" || echo "0")
    FAILED_CHECKS=$(echo "$CHECKS_OUTPUT" | grep -c "fail" || echo "0")
else
    echo "  (체크 없음)"
    PENDING_CHECKS=0
    FAILED_CHECKS=0
fi
echo ""

# 3. 리뷰 코멘트 (인라인)
echo "[리뷰 코멘트]"
COMMENTS=$(gh api "repos/{owner}/{repo}/pulls/$PR_NUMBER/comments" \
    --jq '.[] | "---\n작성자: \(.user.login)\n파일: \(.path):\(.line // .original_line // "?")\n내용: \(.body[0:500])\n"' \
    2>/dev/null || echo "")

if [ -n "$COMMENTS" ]; then
    echo "$COMMENTS" | head -100
else
    echo "  (리뷰 코멘트 없음)"
fi
echo ""

# 4. PR 코멘트 (봇 리뷰 요약 포함)
echo "[PR 코멘트 (최근 5개)]"
gh api "repos/{owner}/{repo}/issues/$PR_NUMBER/comments" \
    --jq '.[-5:] | .[] | "---\n작성자: \(.user.login)\n내용: \(.body[0:500])\n"' \
    2>/dev/null || echo "  (코멘트 없음)"
echo ""

# 5. 판단
echo "[판단]"
REVIEW_STATE=$(gh pr view "$PR_NUMBER" --json reviewDecision -q '.reviewDecision' 2>/dev/null || echo "")
MERGEABLE=$(gh pr view "$PR_NUMBER" --json mergeable -q '.mergeable' 2>/dev/null || echo "")

# P0 코멘트 존재 여부 확인 (자동 병합 차단 기준)
ALL_COMMENTS=$(gh api "repos/{owner}/{repo}/issues/$PR_NUMBER/comments" --jq '.[].body' 2>/dev/null || echo "")
ALL_REVIEW_COMMENTS=$(gh api "repos/{owner}/{repo}/pulls/$PR_NUMBER/comments" --jq '.[].body' 2>/dev/null || echo "")
ALL_TEXT="$ALL_COMMENTS"$'\n'"$ALL_REVIEW_COMMENTS"

P0_COUNT=$(echo "$ALL_TEXT" | grep -c '\[P0' || echo "0")
P1_COUNT=$(echo "$ALL_TEXT" | grep -c '\[P1' || echo "0")
P2_COUNT=$(echo "$ALL_TEXT" | grep -c '\[P2' || echo "0")

echo "  심각도: P0=${P0_COUNT}건, P1=${P1_COUNT}건, P2=${P2_COUNT}건"
echo ""

# 자동 병합 함수
try_auto_merge() {
    if [ "$AUTO_MERGE" != true ]; then
        return 1
    fi

    # P0이 있으면 자동 병합 차단
    if [ "$P0_COUNT" -gt 0 ]; then
        echo "  [AUTO-MERGE] 차단 — P0 이슈 ${P0_COUNT}건이 있습니다. 수정이 필요합니다."
        return 1
    fi

    echo "  [AUTO-MERGE] P0 이슈 없음. 자동 병합을 시도합니다..."
    if gh pr merge "$PR_NUMBER" --squash --delete-branch 2>&1; then
        echo "  [AUTO-MERGE] 병합 완료"
        return 0
    else
        echo "  [AUTO-MERGE] 병합 실패. 수동으로 확인하세요."
        return 1
    fi
}

if [ "$FAILED_CHECKS" -gt 0 ]; then
    echo "  STATUS: CI_FAILED"
    echo "  --> CI 체크 실패. 위 실패 항목을 확인하고 수정 후 재푸시하세요."
    exit 2
elif [ "$PENDING_CHECKS" -gt 0 ]; then
    echo "  STATUS: PENDING"
    echo "  --> CI 체크 진행 중 (${PENDING_CHECKS}건). 완료 후 다시 확인하세요."
    exit 3
elif [ "$REVIEW_STATE" = "CHANGES_REQUESTED" ]; then
    echo "  STATUS: CHANGES_REQUESTED"
    echo "  --> 리뷰에서 변경 요청됨. 위 코멘트를 읽고 수정 후 재푸시하세요."
    exit 2
elif [ "$REVIEW_STATE" = "APPROVED" ] && [ "$MERGEABLE" = "MERGEABLE" ]; then
    echo "  STATUS: READY_TO_MERGE"
    if try_auto_merge; then
        exit 0
    fi
    echo "  --> 병합하려면: gh pr merge $PR_NUMBER --squash"
    exit 0
else
    # 봇 코멘트 확인
    BOT_COMMENT_COUNT=$(gh api "repos/{owner}/{repo}/issues/$PR_NUMBER/comments" \
        --jq "[.[] | select(.user.login | test(\"$KNOWN_BOTS\"; \"i\"))] | length" \
        2>/dev/null || echo "0")

    if [ "$BOT_COMMENT_COUNT" -eq 0 ]; then
        echo "  STATUS: WAITING_FOR_REVIEWS"
        echo "  --> 리뷰 봇 응답 대기 중."
        exit 3
    else
        echo "  STATUS: REVIEWS_RECEIVED"
        echo "  --> 리뷰 ${BOT_COMMENT_COUNT}건 수신됨."

        # P0 없고 auto-merge면 병합 시도
        if try_auto_merge; then
            exit 0
        fi

        if [ "$P0_COUNT" -gt 0 ]; then
            echo "  --> P0 이슈 ${P0_COUNT}건. 반드시 수정 후 재푸시하세요."
            exit 2
        elif [ "$P1_COUNT" -gt 0 ]; then
            echo "  --> P1 이슈 ${P1_COUNT}건. 수정 권장. 판단 후 병합 가능."
            exit 0
        else
            echo "  --> P0/P1 이슈 없음. 병합 가능합니다."
            echo "  --> 병합하려면: gh pr merge $PR_NUMBER --squash"
            exit 0
        fi
    fi
fi
