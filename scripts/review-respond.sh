#!/usr/bin/env bash
# PR 리뷰 코멘트를 확인하고 상태를 출력하는 스크립트
# 사용법: ./scripts/review-respond.sh [PR번호]
#
# 에이전트가 이 출력을 읽고 수정 → 재푸시 → 재실행하는 루프를 돈다

set -euo pipefail

# PR 번호 자동 감지 (현재 브랜치 기준)
if [ -n "${1:-}" ]; then
    PR_NUMBER="$1"
else
    PR_NUMBER=$(gh pr view --json number -q '.number' 2>/dev/null || echo "")
    if [ -z "$PR_NUMBER" ]; then
        echo "[ERROR] 현재 브랜치에 연결된 PR이 없습니다"
        echo "[INFO] 사용법: ./scripts/review-respond.sh <PR번호>"
        exit 1
    fi
fi

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
gh pr checks "$PR_NUMBER" 2>/dev/null || echo "  (체크 없음)"
echo ""

# 3. 리뷰 코멘트 (최근 20개)
echo "[리뷰 코멘트]"
COMMENTS=$(gh api "repos/{owner}/{repo}/pulls/$PR_NUMBER/comments" \
    --jq '.[] | "---\n작성자: \(.user.login)\n파일: \(.path):\(.line // .original_line // "?")\n내용: \(.body)\n"' \
    2>/dev/null || echo "")

if [ -n "$COMMENTS" ]; then
    echo "$COMMENTS" | head -100
else
    echo "  (리뷰 코멘트 없음)"
fi
echo ""

# 4. 일반 코멘트 (봇 리뷰 포함)
echo "[PR 코멘트 (최근 5개)]"
gh api "repos/{owner}/{repo}/issues/$PR_NUMBER/comments" \
    --jq '.[-5:] | .[] | "---\n작성자: \(.user.login)\n내용: \(.body[0:500])\n"' \
    2>/dev/null || echo "  (코멘트 없음)"
echo ""

# 5. 변경 요청 사항 요약
echo "[액션 필요 여부]"
REVIEW_STATE=$(gh pr view "$PR_NUMBER" --json reviewDecision -q '.reviewDecision' 2>/dev/null || echo "")
MERGEABLE=$(gh pr view "$PR_NUMBER" --json mergeable -q '.mergeable' 2>/dev/null || echo "")
CHECKS_PASS=$(gh pr checks "$PR_NUMBER" 2>/dev/null | grep -c "fail" || echo "0")

if [ "$REVIEW_STATE" = "APPROVED" ] && [ "$MERGEABLE" = "MERGEABLE" ] && [ "$CHECKS_PASS" = "0" ]; then
    echo "  --> 병합 가능 상태입니다"
    echo "  --> 병합하려면: gh pr merge $PR_NUMBER --squash"
elif [ "$CHECKS_PASS" != "0" ]; then
    echo "  --> CI 체크 실패. 수정 후 재푸시 필요"
elif [ "$REVIEW_STATE" = "CHANGES_REQUESTED" ]; then
    echo "  --> 리뷰 변경 요청됨. 위 코멘트를 확인하고 수정 후 재푸시하세요"
else
    echo "  --> 리뷰 대기 중"
fi
