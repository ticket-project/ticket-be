#!/usr/bin/env bash
# 앱 + Redis 정리
# 사용법: ./scripts/app-stop.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
PID_FILE="$SCRIPT_DIR/.app.pid"

cd "$PROJECT_ROOT"

# 1. 앱 종료
if [ -f "$PID_FILE" ]; then
    APP_PID=$(cat "$PID_FILE")
    if kill -0 "$APP_PID" 2>/dev/null; then
        echo "[INFO] 앱 종료 중 (PID: $APP_PID)..."
        kill "$APP_PID"
        # graceful shutdown 대기 (최대 10초)
        for i in $(seq 1 10); do
            if ! kill -0 "$APP_PID" 2>/dev/null; then
                break
            fi
            sleep 1
        done
        # 아직 살아있으면 강제 종료
        if kill -0 "$APP_PID" 2>/dev/null; then
            kill -9 "$APP_PID" 2>/dev/null || true
        fi
        echo "[OK] 앱 종료 완료"
    else
        echo "[INFO] 앱이 이미 종료되어 있습니다"
    fi
    rm -f "$PID_FILE"
else
    echo "[INFO] 실행 중인 앱 없음"
fi

# 2. Redis 종료 (선택)
if [ "${KEEP_REDIS:-}" != "true" ]; then
    echo "[INFO] Redis 컨테이너 종료 중..."
    docker compose stop redis 2>/dev/null || docker-compose stop redis 2>/dev/null || true
    echo "[OK] Redis 종료 완료"
else
    echo "[INFO] Redis 유지 (KEEP_REDIS=true)"
fi

# 3. 임시 파일 정리
rm -f "$SCRIPT_DIR/.app.log"
echo "[OK] 정리 완료"
