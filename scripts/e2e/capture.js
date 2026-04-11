/**
 * Playwright 기반 스크린샷 캡처
 *
 * 에이전트가 변경 전/후 상태를 시각적으로 검증할 수 있도록
 * Swagger UI와 주요 API 응답을 스크린샷으로 캡처한다.
 *
 * 사용법: node scripts/e2e/capture.js [--output=./screenshots]
 *
 * 전제 조건:
 *   - 앱이 실행 중 (./scripts/app-start.sh)
 *   - Playwright 설치 (cd scripts/e2e && npm install && npm run install-browsers)
 */

const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';
const OUTPUT_DIR = process.env.OUTPUT_DIR || path.join(__dirname, 'screenshots');
const TIMESTAMP = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);

async function ensureDir(dir) {
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
    }
}

async function capture(page, name, url, options = {}) {
    const filePath = path.join(OUTPUT_DIR, `${TIMESTAMP}_${name}.png`);
    try {
        await page.goto(url, { waitUntil: 'networkidle', timeout: 15000 });

        if (options.waitForSelector) {
            await page.waitForSelector(options.waitForSelector, { timeout: 10000 });
        }
        if (options.delay) {
            await page.waitForTimeout(options.delay);
        }

        await page.screenshot({ path: filePath, fullPage: options.fullPage || false });
        console.log(`  PASS  ${name} → ${path.basename(filePath)}`);
        return true;
    } catch (err) {
        console.log(`  FAIL  ${name} → ${err.message}`);
        return false;
    }
}

async function captureApiResponse(page, name, url, method = 'GET', body = null) {
    const filePath = path.join(OUTPUT_DIR, `${TIMESTAMP}_${name}.png`);
    try {
        // API 응답을 브라우저에서 보기 좋게 렌더링
        const fetchOptions = {
            method,
            headers: { 'Content-Type': 'application/json' },
        };
        if (body) fetchOptions.body = JSON.stringify(body);

        await page.goto('about:blank');
        const response = await page.evaluate(async (opts) => {
            const res = await fetch(opts.url, opts.fetchOptions);
            const text = await res.text();
            return { status: res.status, body: text };
        }, { url, fetchOptions });

        // JSON 포맷팅하여 페이지에 표시
        await page.setContent(`
            <html>
            <head><style>
                body { font-family: monospace; padding: 20px; background: #1e1e1e; color: #d4d4d4; }
                .status { color: ${response.status < 400 ? '#4ec9b0' : '#f44747'}; font-size: 18px; margin-bottom: 10px; }
                .url { color: #569cd6; margin-bottom: 20px; }
                pre { background: #2d2d2d; padding: 16px; border-radius: 4px; overflow: auto; white-space: pre-wrap; }
            </style></head>
            <body>
                <div class="status">${method} ${response.status}</div>
                <div class="url">${url}</div>
                <pre>${formatJson(response.body)}</pre>
            </body>
            </html>
        `.replace('${formatJson(response.body)}', (() => {
            try { return JSON.stringify(JSON.parse(response.body), null, 2); }
            catch { return response.body; }
        })()));

        await page.screenshot({ path: filePath, fullPage: true });
        console.log(`  PASS  ${name} (${response.status}) → ${path.basename(filePath)}`);
        return true;
    } catch (err) {
        console.log(`  FAIL  ${name} → ${err.message}`);
        return false;
    }
}

async function main() {
    await ensureDir(OUTPUT_DIR);

    console.log('=== 스크린샷 캡처 시작 ===');
    console.log(`  URL: ${BASE_URL}`);
    console.log(`  출력: ${OUTPUT_DIR}`);
    console.log('');

    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext({ viewport: { width: 1280, height: 900 } });
    const page = await context.newPage();

    let pass = 0;
    let fail = 0;

    // --- Swagger UI ---
    console.log('[Swagger UI]');
    const swaggerUrl = `${BASE_URL}/api/swagger-ui/index.html`;
    if (await capture(page, 'swagger-overview', swaggerUrl, {
        waitForSelector: '.swagger-ui',
        delay: 2000,
        fullPage: true
    })) pass++; else fail++;

    // --- Health Check ---
    console.log('');
    console.log('[API 응답 캡처]');
    if (await captureApiResponse(page, 'health', `${BASE_URL}/actuator/health`)) pass++; else fail++;

    // --- 공연 목록 ---
    if (await captureApiResponse(page, 'shows-list', `${BASE_URL}/api/v1/shows`)) pass++; else fail++;

    // --- 최신 공연 ---
    if (await captureApiResponse(page, 'shows-latest', `${BASE_URL}/api/v1/shows/latest`)) pass++; else fail++;

    // --- 장르 ---
    if (await captureApiResponse(page, 'genres', `${BASE_URL}/api/v1/genres`)) pass++; else fail++;

    // --- 소셜 로그인 URL ---
    if (await captureApiResponse(page, 'social-urls', `${BASE_URL}/api/v1/auth/social/urls`)) pass++; else fail++;

    // --- OpenAPI Spec ---
    if (await captureApiResponse(page, 'openapi-spec', `${BASE_URL}/api/api-docs`)) pass++; else fail++;

    await browser.close();

    console.log('');
    console.log(`=== 캡처 완료: ${pass} 성공, ${fail} 실패 ===`);
    console.log(`  파일 위치: ${OUTPUT_DIR}/`);

    process.exit(fail > 0 ? 1 : 0);
}

main().catch(err => {
    console.error('[ERROR]', err.message);
    process.exit(1);
});
