#!/bin/bash
set -e

EC2_HOST="15.165.245.144"
EC2_USER="ec2-user"
EC2_KEY="${EC2_KEY:-memory.pem}"
NETWORK_NAME="memory-net"

echo "🚀 EC2 단일 서버 배포 시작..."

ssh -i "$EC2_KEY" "$EC2_USER@$EC2_HOST" << 'EOF'
set -e

NETWORK_NAME="memory-net"
IMAGE_NAME="choehyungwon/memory-app"
IMAGE_TAG="${IMAGE_TAG:-latest}"

echo "1. 환경변수 로드"
if [ -f ~/.env ]; then
  source ~/.env
else
  echo "~/.env 파일이 없습니다"
  exit 1
fi

echo "2. 네트워크 준비"
docker network inspect "$NETWORK_NAME" >/dev/null 2>&1 || docker network create memory-net

echo "3. MySQL 컨테이너 실행"

# 컨테이너 정리
docker stop memory-mysql 2>/dev/null || true
docker rm memory-mysql 2>/dev/null || true

# MySQL 데이터 볼륨 준비 (없으면 자동 생성)
docker volume inspect mysql-data >/dev/null 2>&1 || docker volume create mysql-data

docker run -d \
  --name memory-mysql \
  --network $NETWORK_NAME \
  --restart unless-stopped \
  -v mysql-data:/var/lib/mysql \
  -e MYSQL_ROOT_PASSWORD="\$MYSQL_ROOT_PASSWORD" \
  -e MYSQL_DATABASE="\$MYSQL_DATABASE" \
  -e MYSQL_USER="\$MYSQL_USER" \
  -e MYSQL_PASSWORD="\$MYSQL_PASSWORD" \
  mysql:8.0 \
  --default-authentication-plugin=mysql_native_password

echo "⏳ MySQL 대기 (20초)"
sleep 20

echo "4. 애플리케이션 이미지 pull"
docker pull "$IMAGE_NAME:$IMAGE_TAG"

docker stop memory-app 2>/dev/null || true
docker rm memory-app 2>/dev/null || true

echo "5. 애플리케이션 실행 (8081)"
docker run -d \
  --name memory-app \
  --network "$NETWORK_NAME" \
  --restart unless-stopped \
  -p 8081:8081 \
      -e SPRING_DATASOURCE_URL="jdbc:mysql://memory-mysql:3306/$MYSQL_DATABASE?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true" \
  -e SPRING_DATASOURCE_USERNAME="$MYSQL_USER" \
  -e SPRING_DATASOURCE_PASSWORD="$MYSQL_PASSWORD" \
  "$IMAGE_NAME:$IMAGE_TAG"

echo "⏳ 애플리케이션 대기 (30초)"
sleep 30

echo "6. 상태 확인"
docker ps
echo ""
docker logs memory-app --tail 30
echo ""
curl -f http://localhost:8081/actuator/health || echo "❌ 헬스체크 실패"
EOF

echo ""
echo "✅ 배포 완료"
echo "📍 Swagger: http://15.165.245.144:8081/swagger-ui/index.html"
echo "🔍 Health:  http://15.165.245.144:8081/actuator/health"
echo "⚠️  EC2 보안그룹 8081 포트 오픈 필수"
