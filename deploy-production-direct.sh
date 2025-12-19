#!/bin/bash

EC2_HOST="15.165.245.144"
EC2_USER="ec2-user"
EC2_KEY="${EC2_KEY:-memory.pem}"
NETWORK_NAME="memory-net"

echo "🚀 EC2 단일 서버 배포 시작..."

ssh -i "$EC2_KEY" "$EC2_USER@$EC2_HOST" << 'EOF'
echo "1️⃣ 기존 컨테이너 정리"
docker stop memory-app memory-mysql 2>/dev/null || true
docker rm memory-app memory-mysql 2>/dev/null || true

echo "2️⃣ 네트워크 준비"
docker network inspect memory-net >/dev/null 2>&1 || docker network create memory-net

echo "3️⃣ MySQL 실행"
docker run -d \
  --name memory-mysql \
  --network memory-net \
  --restart unless-stopped \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=memory_db \
  -e MYSQL_USER=memory_user \
  -e MYSQL_PASSWORD=memory_password \
  mysql:8.0 \
  --default-authentication-plugin=mysql_native_password

echo "⏳ MySQL 대기 (20초)"
sleep 20

echo "4️⃣ 애플리케이션 이미지 pull"
docker pull choehyungwon/memory-app:latest

echo "5️⃣ 애플리케이션 실행 (8081)"
docker run -d \
  --name memory-app \
  --network memory-net \
  --restart unless-stopped \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://memory-mysql:3306/memory_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true" \
  -e SPRING_DATASOURCE_USERNAME="memory_user" \
  -e SPRING_DATASOURCE_PASSWORD="memory_password" \
  choehyungwon/memory-app:latest

echo "⏳ 애플리케이션 대기 (30초)"
sleep 30

echo "6️⃣ 상태 확인"
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
