#!/bin/bash

# 설정 변수 (사용자 환경에 맞게 수정 필요)
DOCKER_USERNAME="davidcho7463" # Docker Hub 사용자명
REPO_NAME="mcompany-hub" # Docker Hub에 생성한 리포지토리 이름
TAG="latest"

FULL_IMAGE_NAME="docker.io/$DOCKER_USERNAME/$REPO_NAME:$TAG"
CONTAINER_NAME="teacher-app"

echo "=========================================="
echo "1. 최신 이미지 가져오기 (Pull)..."
echo "=========================================="
docker pull $FULL_IMAGE_NAME

if [ $? -ne 0 ]; then
    echo "이미지 Pull 실패! 'docker login'이 필요한지 확인하세요 (Public 리포지토리는 불필요)."
    exit 1
fi

echo "=========================================="
echo "2. 기존 컨테이너 중지 및 삭제..."
echo "=========================================="
if [ "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
    docker stop $CONTAINER_NAME
fi

if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    docker rm $CONTAINER_NAME
fi

echo "=========================================="
echo "3. 새 컨테이너 실행..."
echo "=========================================="
# 필요한 경우 -e 옵션으로 환경 변수 추가 (예: DB 정보)
docker run -d \
  --name $CONTAINER_NAME \
  --restart always \
  -p 80:8080 \
  -e TZ=Asia/Seoul \
  $FULL_IMAGE_NAME

echo "=========================================="
echo "배포 완료!"
echo "docker logs -f $CONTAINER_NAME 명령어로 로그를 확인할 수 있습니다."
echo "=========================================="
