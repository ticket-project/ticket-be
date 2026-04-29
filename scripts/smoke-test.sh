#!/usr/bin/env bash
# 핵심 API 엔드포인트 스모크 테스트
# 사용법: ./scripts/smoke-test.sh
#
# 앱이 실행 중이어야 함 (./scripts/app-start.sh)
# 종료 코드: 0=성공, 1=실패

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
PASS=0
FAIL=0
TOTAL=0

check() {
    local description="$1"
    local method="$2"
    local url="$3"
    local expected_status="${4:-200}"

    TOTAL=$((TOTAL + 1))
    local status
    status=$(curl -sf -o /dev/null -w "%{http_code}" -X "$method" "$url" 2>/dev/null || echo "000")

    if [ "$status" = "$expected_status" ]; then
        echo "  PASS  $description ($status)"
        PASS=$((PASS + 1))
    else
        echo "  FAIL  $description (expected=$expected_status, got=$status)"
        FAIL=$((FAIL + 1))
    fi
}

check_json() {
    local description="$1"
    local url="$2"
    local json_path="$3"

    TOTAL=$((TOTAL + 1))
    local response
    response=$(curl -sf "$url" 2>/dev/null || echo "")

    if [ -z "$response" ]; then
        echo "  FAIL  $description (응답 없음)"
        FAIL=$((FAIL + 1))
        return
    fi

    # node로 JSON 필드 존재 확인
    local result
    result=$(echo "$response" | node -e "
        const data = JSON.parse(require('fs').readFileSync(0, 'utf8'));
        const path = '$json_path'.split('.');
        let val = data;
        for (const key of path) { val = val?.[key]; }
        console.log(val !== undefined && val !== null ? 'OK' : 'MISSING');
    " 2>/dev/null || echo "ERROR")

    if [ "$result" = "OK" ]; then
        echo "  PASS  $description ($.${json_path} 존재)"
        PASS=$((PASS + 1))
    else
        echo "  FAIL  $description ($.${json_path} 누락)"
        FAIL=$((FAIL + 1))
    fi
}

echo "=== Smoke Test: $BASE_URL ==="
echo ""

# --- 인프라 ---
echo "[인프라]"
check "Health endpoint" GET "$BASE_URL/actuator/health"
check "Prometheus metrics" GET "$BASE_URL/actuator/prometheus"
check "Swagger UI" GET "$BASE_URL/api/swagger-ui.html" "302"
check "OpenAPI docs" GET "$BASE_URL/api/api-docs"

# --- 공개 API: 공연 ---
echo ""
echo "[공연 조회 API]"
check "공연 목록 조회" GET "$BASE_URL/api/v1/shows"
check "최신 공연 조회" GET "$BASE_URL/api/v1/shows/latest"
check "오픈 예정 공연" GET "$BASE_URL/api/v1/shows/sale-opening-soon"
check "공연 검색" GET "$BASE_URL/api/v1/shows/search?keyword=test"
check "공연 검색 카운트" GET "$BASE_URL/api/v1/shows/search/count?keyword=test"

# --- 공개 API: 메타데이터 ---
echo ""
echo "[메타데이터 API]"
check "장르 조회" GET "$BASE_URL/api/v1/genres"
check "메타 코드 조회" GET "$BASE_URL/api/v1/meta/codes"

# --- 인증 API ---
echo ""
echo "[인증 API]"
check "소셜 로그인 URL 조회" GET "$BASE_URL/api/v1/auth/social/urls"
check "미인증 접근 차단 (주문)" GET "$BASE_URL/api/v1/orders" "401"
check "미인증 접근 차단 (회원)" GET "$BASE_URL/api/v1/members" "401"

# --- 인증 후 API (회원가입 → 로그인 → 토큰 획득) ---
echo ""
echo "[인증 흐름 테스트]"
SIGNUP_RESPONSE=$(curl -sf -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/auth/signup" \
    -H "Content-Type: application/json" \
    -d '{"email":"smoke-test@test.com","password":"Test1234!@","name":"스모크테스트"}' 2>/dev/null || echo -e "\n000")

SIGNUP_STATUS=$(echo "$SIGNUP_RESPONSE" | tail -1)

# 이미 가입된 경우(409) 또는 성공(200/201) 모두 허용
if [ "$SIGNUP_STATUS" = "200" ] || [ "$SIGNUP_STATUS" = "201" ] || [ "$SIGNUP_STATUS" = "409" ]; then
    echo "  PASS  회원가입 ($SIGNUP_STATUS)"
    PASS=$((PASS + 1))
else
    echo "  FAIL  회원가입 (got=$SIGNUP_STATUS)"
    FAIL=$((FAIL + 1))
fi
TOTAL=$((TOTAL + 1))

LOGIN_RESPONSE=$(curl -sf -X POST "$BASE_URL/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"smoke-test@test.com","password":"Test1234!@"}' 2>/dev/null || echo "")

if [ -n "$LOGIN_RESPONSE" ]; then
    ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | node -e "
        const data = JSON.parse(require('fs').readFileSync(0, 'utf8'));
        console.log(data.accessToken || data.access_token || data.token || '');
    " 2>/dev/null || echo "")

    if [ -n "$ACCESS_TOKEN" ]; then
        echo "  PASS  로그인 + 토큰 발급"
        PASS=$((PASS + 1))
        TOTAL=$((TOTAL + 1))

        # 인증 후 엔드포인트 테스트
        MEMBER_STATUS=$(curl -sf -o /dev/null -w "%{http_code}" \
            -H "Authorization: Bearer $ACCESS_TOKEN" \
            "$BASE_URL/api/v1/members" 2>/dev/null || echo "000")
        TOTAL=$((TOTAL + 1))
        if [ "$MEMBER_STATUS" = "200" ]; then
            echo "  PASS  인증 후 회원 정보 조회 ($MEMBER_STATUS)"
            PASS=$((PASS + 1))
        else
            echo "  FAIL  인증 후 회원 정보 조회 (expected=200, got=$MEMBER_STATUS)"
            FAIL=$((FAIL + 1))
        fi
    else
        echo "  FAIL  로그인 응답에서 토큰 추출 실패"
        FAIL=$((FAIL + 1))
        TOTAL=$((TOTAL + 1))
    fi
else
    echo "  FAIL  로그인 요청 실패"
    FAIL=$((FAIL + 1))
    TOTAL=$((TOTAL + 1))
fi

# --- 결과 ---
echo ""
echo "==============================="
echo "  총: $TOTAL  |  성공: $PASS  |  실패: $FAIL"
echo "==============================="

if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
