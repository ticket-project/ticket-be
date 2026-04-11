#!/usr/bin/env bash
# doc-gardening: 문서 정합성 검사 + 드리프트 감지
#
# 사용법:
#   ./scripts/doc-gardening.sh          # 검사만
#   ./scripts/doc-gardening.sh --fix    # 검사 + 자동 수정 PR 생성 (에이전트용)
#
# 검사 항목:
#   1. 필수 문서 존재 확인
#   2. 교차 링크 유효성
#   3. CLAUDE.md 크기 경고
#   4. docs/ARCHITECTURE.md와 실제 패키지 구조 비교
#   5. docs/QUALITY_SCORE.md 업데이트 필요 여부
#   6. 삭제된 도메인/패키지가 문서에 남아있는지

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
FIX_MODE="${1:-}"

cd "$PROJECT_ROOT"

ISSUES=()
WARNINGS=()

issue() { ISSUES+=("$1"); echo "  ISSUE  $1"; }
warn()  { WARNINGS+=("$1"); echo "  WARN   $1"; }
ok()    { echo "  OK     $1"; }

echo "=== Doc Gardening: 문서 정합성 검사 ==="
echo ""

# ──────────────────────────────────────────────
# 1. 필수 문서 존재
# ──────────────────────────────────────────────
echo "[1. 필수 문서]"
REQUIRED_FILES=(
    "CLAUDE.md"
    "AGENTS.md"
    "docs/ARCHITECTURE.md"
    "docs/PRODUCT_SENSE.md"
    "docs/RELIABILITY.md"
    "docs/QUALITY_SCORE.md"
    "docs/design-docs/core-beliefs.md"
    "docs/exec-plans/README.md"
    "docs/references/domain-glossary.md"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        ok "$file"
    else
        issue "$file 가 존재하지 않습니다"
    fi
done
echo ""

# ──────────────────────────────────────────────
# 2. 교차 링크 유효성
# ──────────────────────────────────────────────
echo "[2. 교차 링크]"
LINK_OK=true
for md_file in CLAUDE.md AGENTS.md $(find docs -name "*.md" -type f 2>/dev/null); do
    [ -f "$md_file" ] || continue
    # 마크다운 링크 추출: [text](path) → path
    LINKS=$(sed -n 's/.*\](\([^)]*\)).*/\1/p' "$md_file" 2>/dev/null || true)
    for link in $LINKS; do
        # 외부 URL, 앵커 스킵
        case "$link" in
            http*|https*|\#*) continue ;;
        esac
        # 앵커 제거
        clean_link="${link%%#*}"
        [ -z "$clean_link" ] && continue

        DIR=$(dirname "$md_file")
        if [ "$DIR" = "." ]; then
            RESOLVED="$clean_link"
        else
            RESOLVED="$DIR/$clean_link"
        fi

        if [ ! -f "$RESOLVED" ] && [ ! -d "$RESOLVED" ]; then
            issue "$md_file → 깨진 링크: $link"
            LINK_OK=false
        fi
    done
done
[ "$LINK_OK" = true ] && ok "모든 교차 링크 유효"
echo ""

# ──────────────────────────────────────────────
# 3. CLAUDE.md 크기
# ──────────────────────────────────────────────
echo "[3. CLAUDE.md 크기]"
if [ -f "CLAUDE.md" ]; then
    LINE_COUNT=$(wc -l < CLAUDE.md)
    if [ "$LINE_COUNT" -gt 200 ]; then
        warn "CLAUDE.md가 ${LINE_COUNT}줄입니다 (권장 200줄 이내). 맵은 간결해야 합니다."
    else
        ok "CLAUDE.md ${LINE_COUNT}줄"
    fi
fi
echo ""

# ──────────────────────────────────────────────
# 4. 도메인 패키지 vs 문서 정합성
# ──────────────────────────────────────────────
echo "[4. 도메인 패키지 ↔ 문서]"
DOMAIN_ROOT="core/core-domain/src/main/java/com/ticket/core/domain"

if [ -d "$DOMAIN_ROOT" ]; then
    # 실제 존재하는 도메인 패키지
    ACTUAL_DOMAINS=$(ls -d "$DOMAIN_ROOT"/*/ 2>/dev/null | xargs -I{} basename {} | sort)

    # CLAUDE.md에 언급된 도메인
    for domain in $ACTUAL_DOMAINS; do
        if grep -q "$domain" CLAUDE.md 2>/dev/null; then
            ok "도메인 '$domain' → CLAUDE.md에 문서화됨"
        else
            warn "도메인 '$domain'이 CLAUDE.md에 언급되지 않았습니다"
        fi
    done

    # 문서에는 있지만 실제로는 없는 도메인
    DOCUMENTED_DOMAINS=$(grep -o '`[a-z]*`' CLAUDE.md 2>/dev/null | tr -d '`' | sort -u)
    for domain in $DOCUMENTED_DOMAINS; do
        if [ ! -d "$DOMAIN_ROOT/$domain" ]; then
            issue "CLAUDE.md에 '$domain' 도메인이 있지만, 실제 패키지가 존재하지 않습니다"
        fi
    done
fi
echo ""

# ──────────────────────────────────────────────
# 5. QUALITY_SCORE.md 업데이트 날짜
# ──────────────────────────────────────────────
echo "[5. QUALITY_SCORE.md 신선도]"
if [ -f "docs/QUALITY_SCORE.md" ]; then
    LAST_UPDATE=$(grep '마지막 업데이트:' docs/QUALITY_SCORE.md 2>/dev/null | sed 's/.*마지막 업데이트: //' | sed 's/[^0-9-]//g' || echo "")
    if [ -n "$LAST_UPDATE" ]; then
        DAYS_AGO=$(( ($(date +%s) - $(date -d "$LAST_UPDATE" +%s 2>/dev/null || echo "0")) / 86400 ))
        if [ "$DAYS_AGO" -gt 30 ]; then
            warn "QUALITY_SCORE.md가 ${DAYS_AGO}일 전에 마지막으로 업데이트되었습니다 (30일 초과)"
        else
            ok "QUALITY_SCORE.md 업데이트 ${DAYS_AGO}일 전"
        fi
    else
        warn "QUALITY_SCORE.md에 '마지막 업데이트' 날짜가 없습니다"
    fi
fi
echo ""

# ──────────────────────────────────────────────
# 결과
# ──────────────────────────────────────────────
echo "==============================="
echo "  이슈: ${#ISSUES[@]}건  |  경고: ${#WARNINGS[@]}건"
echo "==============================="

if [ "${#ISSUES[@]}" -gt 0 ] || [ "${#WARNINGS[@]}" -gt 0 ]; then
    echo ""
    echo "[발견된 문제]"
    for i in "${ISSUES[@]}"; do echo "  - ISSUE: $i"; done
    for w in "${WARNINGS[@]}"; do echo "  - WARN: $w"; done
fi

# --fix 모드: 에이전트에게 수정을 지시하는 요약 출력
if [ "$FIX_MODE" = "--fix" ] && [ "${#ISSUES[@]}" -gt 0 ]; then
    echo ""
    echo "[에이전트 수정 지시]"
    echo "다음 이슈를 수정하고 커밋하세요:"
    for i in "${ISSUES[@]}"; do echo "  - $i"; done
    echo ""
    echo "수정 후 검증: ./scripts/doc-gardening.sh"
fi

if [ "${#ISSUES[@]}" -gt 0 ]; then
    exit 1
fi
