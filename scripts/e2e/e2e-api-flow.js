/**
 * E2E API 흐름 테스트
 *
 * 실제 앱에 HTTP 요청을 보내 전체 예매 흐름을 검증한다.
 * Playwright 브라우저가 필요 없는 순수 API 테스트.
 *
 * 사용법: node scripts/e2e/e2e-api-flow.js
 *
 * 전제 조건: 앱이 실행 중 (./scripts/app-start.sh)
 */

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';

let pass = 0;
let fail = 0;
let accessToken = null;

async function request(method, path, options = {}) {
    const url = `${BASE_URL}${path}`;
    const headers = { 'Content-Type': 'application/json', ...options.headers };
    if (options.auth && accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    const fetchOptions = { method, headers };
    if (options.body) {
        fetchOptions.body = JSON.stringify(options.body);
    }

    const response = await fetch(url, fetchOptions);
    const text = await response.text();
    let json = null;
    try { json = JSON.parse(text); } catch {}

    return { status: response.status, json, text };
}

function check(description, condition) {
    if (condition) {
        console.log(`  PASS  ${description}`);
        pass++;
    } else {
        console.log(`  FAIL  ${description}`);
        fail++;
    }
}

async function main() {
    console.log(`=== E2E API Flow Test: ${BASE_URL} ===`);
    console.log('');

    // === 1. 헬스 체크 ===
    console.log('[1. 헬스 체크]');
    const health = await request('GET', '/actuator/health');
    check('서버 정상 응답', health.status === 200);
    check('상태 UP', health.json?.status === 'UP');
    console.log('');

    // === 2. 공개 API 조회 ===
    console.log('[2. 공개 API]');
    const genres = await request('GET', '/api/v1/genres');
    check('장르 목록 조회', genres.status === 200);

    const shows = await request('GET', '/api/v1/shows');
    check('공연 목록 조회', shows.status === 200);

    const latest = await request('GET', '/api/v1/shows/latest');
    check('최신 공연 조회', latest.status === 200);

    const meta = await request('GET', '/api/v1/meta/codes');
    check('메타 코드 조회', meta.status === 200);
    console.log('');

    // === 3. 인증 흐름 ===
    console.log('[3. 인증 흐름]');
    const email = `e2e-${Date.now()}@test.com`;
    const password = 'TestPass1234!@';

    const signup = await request('POST', '/api/v1/auth/signup', {
        body: { email, password, name: 'E2E테스트' }
    });
    check('회원가입', signup.status === 200 || signup.status === 201);

    const login = await request('POST', '/api/v1/auth/login', {
        body: { email, password }
    });
    check('로그인', login.status === 200);

    accessToken = login.json?.accessToken || login.json?.access_token || login.json?.token;
    check('토큰 발급', !!accessToken);
    console.log('');

    // === 4. 인증 후 API ===
    console.log('[4. 인증 후 API]');
    if (accessToken) {
        const member = await request('GET', '/api/v1/members', { auth: true });
        check('회원 정보 조회', member.status === 200);

        const likes = await request('GET', '/api/v1/members/me/likes', { auth: true });
        check('좋아요 목록 조회', likes.status === 200);

        // 미인증 접근 확인
        const noAuth = await request('GET', '/api/v1/members');
        check('미인증 시 401 반환', noAuth.status === 401);
    } else {
        console.log('  SKIP  토큰 없음 — 인증 후 테스트 스킵');
    }
    console.log('');

    // === 5. 공연 상세 (시드 데이터 의존) ===
    console.log('[5. 공연 상세 조회]');
    if (shows.json?.content?.length > 0 || (Array.isArray(shows.json) && shows.json.length > 0)) {
        const showList = shows.json?.content || shows.json;
        const firstShow = showList[0];
        const showId = firstShow?.id || firstShow?.showId;

        if (showId) {
            const detail = await request('GET', `/api/v1/shows/${showId}`);
            check(`공연 상세 조회 (ID: ${showId})`, detail.status === 200);
        } else {
            console.log('  SKIP  공연 ID 추출 불가');
        }
    } else {
        console.log('  SKIP  시드 데이터 없음 — 공연 상세 테스트 스킵');
    }
    console.log('');

    // === 6. 대기열 (시드 데이터 + 인증 의존) ===
    console.log('[6. 대기열 흐름]');
    if (accessToken && shows.json?.content?.length > 0) {
        // 대기열 테스트는 performanceId가 필요 — 시드 데이터에 따라 다름
        console.log('  SKIP  대기열 E2E는 performanceId가 필요 — 시드 데이터 구조 확인 필요');
    } else {
        console.log('  SKIP  인증 또는 시드 데이터 없음');
    }
    console.log('');

    // === 결과 ===
    console.log('===============================');
    console.log(`  총: ${pass + fail}  |  성공: ${pass}  |  실패: ${fail}`);
    console.log('===============================');

    process.exit(fail > 0 ? 1 : 0);
}

main().catch(err => {
    console.error('[ERROR]', err.message);
    process.exit(1);
});
