#!/usr/bin/env bash
# 에이전트/개발자가 로컬에서 앱을 부팅하는 스크립트
# 사용법: ./scripts/app-start.sh
#
# 전제 조건: Docker가 실행 중이어야 함 (Redis 컨테이너)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$SCRIPT_DIR/.env.local"
PID_FILE="$SCRIPT_DIR/.app.pid"
LOG_FILE="$SCRIPT_DIR/.app.log"

cd "$PROJECT_ROOT"

# 이미 실행 중인지 확인
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if kill -0 "$OLD_PID" 2>/dev/null; then
        echo "[INFO] 앱이 이미 실행 중입니다 (PID: $OLD_PID)"
        echo "[INFO] 종료하려면: ./scripts/app-stop.sh"
        exit 0
    else
        rm -f "$PID_FILE"
    fi
fi

# 1. 환경 변수 로드
if [ -f "$ENV_FILE" ]; then
    echo "[INFO] 환경 변수 로드: $ENV_FILE"
    set -a
    source "$ENV_FILE"
    set +a
else
    echo "[ERROR] $ENV_FILE 파일이 없습니다"
    exit 1
fi

# 2. Redis 컨테이너 시작
echo "[INFO] Redis 컨테이너 시작..."
docker compose up -d redis 2>/dev/null || docker-compose up -d redis 2>/dev/null

# Redis 헬스 체크 대기
echo "[INFO] Redis 준비 대기 중..."
for i in $(seq 1 30); do
    if docker exec ticket-redis redis-cli ping 2>/dev/null | grep -q PONG; then
        echo "[INFO] Redis 준비 완료"
        break
    fi
    if [ "$i" -eq 30 ]; then
        echo "[ERROR] Redis 시작 실패 (30초 타임아웃)"
        exit 1
    fi
    sleep 1
done

# 3. 앱 빌드
echo "[INFO] 앱 빌드 중..."
./gradlew :core:core-api:bootJar -x test --quiet

# 4. 앱 백그라운드 시작
echo "[INFO] 앱 시작 중 (local 프로필)..."
java -jar core/core-api/build/libs/*.jar \
    --spring.profiles.active=local \
    > "$LOG_FILE" 2>&1 &

APP_PID=$!
echo "$APP_PID" > "$PID_FILE"

# 5. 헬스 체크 대기
echo "[INFO] 앱 준비 대기 중 (PID: $APP_PID)..."
for i in $(seq 1 60); do
    if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "[OK] 앱 시작 완료 — http://localhost:8080"
        echo "[OK] Swagger UI — http://localhost:8080/api/swagger-ui.html"
        echo "[OK] H2 Console — http://localhost:8080/h2-console"
        echo "[OK] 로그: $LOG_FILE"
        exit 0
    fi
    # 프로세스가 죽었는지 확인
    if ! kill -0 "$APP_PID" 2>/dev/null; then
        echo "[ERROR] 앱 시작 실패. 로그 확인:"
        tail -30 "$LOG_FILE"
        rm -f "$PID_FILE"
        exit 1
    fi
    sleep 1
done

echo "[ERROR] 앱 시작 타임아웃 (60초). 로그 확인:"
tail -30 "$LOG_FILE"
kill "$APP_PID" 2>/dev/null || true
rm -f "$PID_FILE"
exit 1
