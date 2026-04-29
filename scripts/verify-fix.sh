#!/usr/bin/env bash
# 에이전트용 전체 검증 파이프라인
# 사용법: ./scripts/verify-fix.sh
#
# 실행 흐름:
#   1. 컴파일 체크
#   2. 아키텍처 + 도메인 테스트
#   3. 앱 부팅
#   4. 스모크 테스트
#   5. (Phase 3) 스크린샷 캡처
#   6. 정리
#
# 종료 코드: 0=모두 통과, 1=실패

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SKIP_BOOT="${SKIP_BOOT:-false}"
CAPTURE_SCREENSHOTS="${CAPTURE_SCREENSHOTS:-false}"

cd "$PROJECT_ROOT"

cleanup() {
    echo ""
    echo "[Step 6] 정리..."
    "$SCRIPT_DIR/app-stop.sh" 2>/dev/null || true
}
trap cleanup EXIT

echo "============================================"
echo "  에이전트 검증 파이프라인"
echo "============================================"
echo ""

# Step 1: 컴파일
echo "[Step 1] 컴파일 체크..."
if ./gradlew :core:core-api:compileJava --quiet 2>&1; then
    echo "  PASS  컴파일 성공"
else
    echo "  FAIL  컴파일 실패"
    exit 1
fi
echo ""

# Step 2: 테스트
echo "[Step 2] 아키텍처 + 도메인 테스트..."
if ./gradlew :core:core-domain:test --quiet 2>&1; then
    echo "  PASS  모든 테스트 통과"
else
    echo "  FAIL  테스트 실패 — 리포트 확인: core/core-domain/build/reports/tests/test/index.html"
    exit 1
fi
echo ""

# Step 3: 앱 부팅
if [ "$SKIP_BOOT" = "true" ]; then
    echo "[Step 3] 앱 부팅 스킵 (SKIP_BOOT=true)"
else
    echo "[Step 3] 앱 부팅..."
    if "$SCRIPT_DIR/app-start.sh"; then
        echo "  PASS  앱 부팅 성공"
    else
        echo "  FAIL  앱 부팅 실패"
        exit 1
    fi
fi
echo ""

# Step 4: 스모크 테스트
if [ "$SKIP_BOOT" = "true" ]; then
    echo "[Step 4] 스모크 테스트 스킵 (앱 미부팅)"
else
    echo "[Step 4] 스모크 테스트..."
    if "$SCRIPT_DIR/smoke-test.sh"; then
        echo ""
        echo "  PASS  스모크 테스트 통과"
    else
        echo ""
        echo "  FAIL  스모크 테스트 실패"
        exit 1
    fi
fi
echo ""

# Step 5: 스크린샷 캡처 (Phase 3)
if [ "$CAPTURE_SCREENSHOTS" = "true" ] && [ "$SKIP_BOOT" != "true" ]; then
    echo "[Step 5] 스크린샷 캡처..."
    if [ -f "$SCRIPT_DIR/e2e/capture.js" ]; then
        if node "$SCRIPT_DIR/e2e/capture.js"; then
            echo "  PASS  스크린샷 캡처 완료"
        else
            echo "  WARN  스크린샷 캡처 실패 (비필수)"
        fi
    else
        echo "  SKIP  e2e/capture.js 없음"
    fi
else
    echo "[Step 5] 스크린샷 스킵"
fi

echo ""
echo "============================================"
echo "  모든 검증 통과"
echo "============================================"
