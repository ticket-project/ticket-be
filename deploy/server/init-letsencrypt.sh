#!/bin/bash
# ============================================
# Let's Encrypt 초기 인증서 발급 스크립트
# 서버에서 최초 1회만 실행하면 됩니다.
# ============================================

set -e

DOMAIN="oneticket.site"
EMAIL="mn040820@naver.com"
DEPLOY_DIR="/home/ubuntu"

echo ">>> 1. 기존 컨테이너 정리"
sudo docker compose -f $DEPLOY_DIR/docker-compose.yml down 2>/dev/null || true

echo ">>> 2. Nginx를 HTTP 전용 모드로 임시 실행"
sudo docker run -d --name temp-nginx \
  -p 80:80 \
  -v certbot-webroot:/var/www/certbot \
  nginx:1.27-alpine \
  sh -c "echo 'server { listen 80; server_name $DOMAIN; location /.well-known/acme-challenge/ { root /var/www/certbot; } location / { return 200 ok; } }' > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"

echo ">>> 3. 10초 대기 (Nginx 시작 대기)"
sleep 10

echo ">>> 4. Let's Encrypt 인증서 발급"
sudo docker run --rm \
  -v certbot-etc:/etc/letsencrypt \
  -v certbot-var:/var/lib/letsencrypt \
  -v certbot-webroot:/var/www/certbot \
  certbot/certbot certonly \
  --webroot \
  --webroot-path=/var/www/certbot \
  --email $EMAIL \
  --agree-tos \
  --no-eff-email \
  -d $DOMAIN

echo ">>> 5. 임시 Nginx 정리"
sudo docker rm -f temp-nginx

echo ">>> 6. 인증서 발급 확인"
sudo docker run --rm \
  -v certbot-etc:/etc/letsencrypt:ro \
  alpine ls -la /etc/letsencrypt/live/$DOMAIN/

echo ""
echo "============================================"
echo "  SSL 인증서 발급 완료!"
echo "  이제 CI/CD로 배포하면 HTTPS가 적용됩니다."
echo "============================================"
