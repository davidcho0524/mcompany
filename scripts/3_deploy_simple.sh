#!/bin/bash

echo "=========================================="
echo "1. 새 이미지 받기 (Update)"
echo "=========================================="
docker pull davidcho7463/mcompany-hub:latest

echo "=========================================="
echo "2. 기존 꺼 끄고 지우기"
echo "=========================================="
# -f 옵션은 컨테이너가 실행 중이더라도 강제로 종료하고 삭제합니다.
docker rm -f teacher-app

echo "=========================================="
echo "3. 새 버전 실행"
echo "=========================================="
# 앞서 논의한 한국 시간대 설정(-e TZ=Asia/Seoul)을 필수로 추가해 두었습니다.
docker run -d \
  --name teacher-app \
  --restart always \
  -p 8080:8080 \
  -e TZ=Asia/Seoul \
  davidcho7463/mcompany-hub:latest

echo "=========================================="
echo "배포가 완료되었습니다!"d
echo "로그 확인: docker logs -f teacher-app"
echo "=========================================="
