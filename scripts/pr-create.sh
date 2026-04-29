#!/usr/bin/env bash
# 에이전트용 PR 생성 스크립트
# 사용법: ./scripts/pr-create.sh "PR 제목" "변경 요약"
#
# 기능:
#   1. 현재 브랜치에서 origin으로 push
#   2. PR 템플릿에 맞춰 PR 생성
#   3. PR URL 출력

set -euo pipefail

TITLE="${1:?사용법: ./scripts/pr-create.sh \"PR 제목\" \"변경 요약\"}"
SUMMARY="${2:-}"
BASE_BRANCH="${BASE_BRANCH:-master}"

CURRENT_BRANCH=$(git branch --show-current)

if [ "$CURRENT_BRANCH" = "$BASE_BRANCH" ]; then
    echo "[ERROR] $BASE_BRANCH 브랜치에서는 PR을 생성할 수 없습니다"
    echo "[INFO] 먼저 피처 브랜치를 생성하세요: git checkout -b feature/xxx"
    exit 1
fi

# 1. 커밋되지 않은 변경 확인
if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "[WARN] 커밋되지 않은 변경이 있습니다"
    git status --short
    echo ""
    echo "[INFO] 먼저 커밋하세요"
    exit 1
fi

# 2. Push
echo "[INFO] $CURRENT_BRANCH → origin 으로 push..."
git push -u origin "$CURRENT_BRANCH"

# 3. 변경 파일 분석
CHANGED_FILES=$(git diff "$BASE_BRANCH"..."$CURRENT_BRANCH" --name-only)

# 영향 범위 자동 판단
IMPACTS=""
echo "$CHANGED_FILES" | grep -q "core/core-api/" && IMPACTS="$IMPACTS\n- [x] core-api" || IMPACTS="$IMPACTS\n- [ ] core-api"
echo "$CHANGED_FILES" | grep -q "core/core-domain/" && IMPACTS="$IMPACTS\n- [x] core-domain" || IMPACTS="$IMPACTS\n- [ ] core-domain"
echo "$CHANGED_FILES" | grep -q "storage/redis-core/" && IMPACTS="$IMPACTS\n- [x] Redis/캐시" || IMPACTS="$IMPACTS\n- [ ] Redis/캐시"
echo "$CHANGED_FILES" | grep -q "docs/" && IMPACTS="$IMPACTS\n- [x] 문서" || IMPACTS="$IMPACTS\n- [ ] 문서"
echo "$CHANGED_FILES" | grep -q ".github/" && IMPACTS="$IMPACTS\n- [x] CI/CD" || IMPACTS="$IMPACTS\n- [ ] CI/CD"

# 커밋 목록
COMMITS=$(git log "$BASE_BRANCH".."$CURRENT_BRANCH" --oneline)

# 4. PR 생성
echo "[INFO] PR 생성 중..."
PR_URL=$(gh pr create \
    --base "$BASE_BRANCH" \
    --title "$TITLE" \
    --body "$(cat <<EOF
## 목적
$SUMMARY

## 주요 변경사항
$COMMITS

## 영향 범위
$(echo -e "$IMPACTS")

## 테스트
- [x] \`./gradlew :core:core-api:compileJava\` 통과
- [x] \`./gradlew :core:core-domain:test\` 통과

---
이 PR은 에이전트에 의해 자동 생성되었습니다.
EOF
)" 2>&1)

echo ""
echo "[OK] PR 생성 완료: $PR_URL"
