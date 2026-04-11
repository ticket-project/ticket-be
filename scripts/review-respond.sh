#!/usr/bin/env bash
# PR 리뷰 상태 확인 + 대기 모드 지원
#
# 사용법:
#   ./scripts/review-respond.sh              # 현재 상태만 확인
#   ./scripts/review-respond.sh --wait       # 리뷰 도착할 때까지 대기 후 확인
#   ./scripts/review-respond.sh --wait 42    # PR #42에 대해 대기
#
# 종료 코드:
#   0 = 병합 가능 (APPROVED + CI 통과)
#   1 = 에러
#   2 = 변경 요청됨 (수정 필요)
#   3 = 아직 대기 중 (리뷰 미완료)

set -euo pipefail

WAIT_MODE=false
PR_NUMBER=""

# 인자 파싱
for arg in "$@"; do
    case "$arg" in
        --wait) WAIT_MODE=true ;;
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

if [ "$FAILED_CHECKS" -gt 0 ]; then
    echo "  STATUS: CI_FAILED"
    echo "  --> CI 체크 실패. 위 실패 항목을 확인하고 수정 후 재푸시하세요."
    exit 2
elif [ "$PENDING_CHECKS" -gt 0 ]; then
    echo "  STATUS: PENDING"
    echo "  --> CI 체크 진행 중 (${PENDING_CHECKS}건). 완료 후 다시 확인하세요."
    echo "  --> 대기 모드: ./scripts/review-respond.sh --wait $PR_NUMBER"
    exit 3
elif [ "$REVIEW_STATE" = "CHANGES_REQUESTED" ]; then
    echo "  STATUS: CHANGES_REQUESTED"
    echo "  --> 리뷰에서 변경 요청됨. 위 코멘트를 읽고 수정 후 재푸시하세요."
    exit 2
elif [ "$REVIEW_STATE" = "APPROVED" ] && [ "$MERGEABLE" = "MERGEABLE" ]; then
    echo "  STATUS: READY_TO_MERGE"
    echo "  --> 병합 가능 상태입니다."
    echo "  --> 병합하려면: gh pr merge $PR_NUMBER --squash"
    exit 0
else
    # 봇 코멘트 확인
    BOT_COMMENT_COUNT=$(gh api "repos/{owner}/{repo}/issues/$PR_NUMBER/comments" \
        --jq "[.[] | select(.user.login | test(\"$KNOWN_BOTS\"; \"i\"))] | length" \
        2>/dev/null || echo "0")

    if [ "$BOT_COMMENT_COUNT" -eq 0 ]; then
        echo "  STATUS: WAITING_FOR_REVIEWS"
        echo "  --> 리뷰 봇 응답 대기 중 (아직 코멘트 없음)."
        echo "  --> 대기 모드: ./scripts/review-respond.sh --wait $PR_NUMBER"
        exit 3
    else
        echo "  STATUS: REVIEWS_RECEIVED"
        echo "  --> 리뷰 ${BOT_COMMENT_COUNT}건 수신됨. 위 코멘트를 확인하세요."
        echo "  --> 추가 변경 없이 병합 가능하면: gh pr merge $PR_NUMBER --squash"
        exit 0
    fi
fi
