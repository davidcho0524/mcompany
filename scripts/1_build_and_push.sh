#!/bin/bash

# 설정 변수 (사용자 환경에 맞게 수정 필요)
DOCKER_USERNAME="davidcho7463" # Docker Hub 사용자명
REPO_NAME="mcompany-hub" # Docker Hub에 생성한 리포지토리 이름
TAG="latest" # 또는 $(date +%Y%m%d-%H%M%S)

FULL_IMAGE_NAME="docker.io/$DOCKER_USERNAME/$REPO_NAME:$TAG"

echo "=========================================="
echo "1. Spring Boot 애플리케이션 빌드 중..."
echo "=========================================="
./gradlew clean bootJar -x test

if [ $? -ne 0 ]; then
    echo "Gradle 빌드 실패!"
    exit 1
fi

echo "=========================================="
echo "2. Docker 이미지 빌드 중 (platform: linux/amd64)..."
echo "=========================================="
# Mac(M1/M2) 사용자를 위해 --platform linux/amd64 옵션 추가
docker build --platform linux/amd64 -t $FULL_IMAGE_NAME .

if [ $? -ne 0 ]; then
    echo "Docker 빌드 실패!"
    exit 1
fi

echo "=========================================="
echo "3. Docker Hub로 푸시 중..."
echo "=========================================="
# 먼저 docker login이 되어 있어야 합니다.
docker push $FULL_IMAGE_NAME

if [ $? -ne 0 ]; then
    echo "Docker 푸시 실패! 'docker login' 명령어로 로그인이 되어 있는지 확인하세요."
    exit 1
fi

echo "=========================================="
echo "빌드 및 푸시 완료!"
echo "이미지: $FULL_IMAGE_NAME"
echo "=========================================="
