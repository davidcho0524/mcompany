# Google Cloud Platform (GCP) 수동 배포 가이드 (Docker Hub)

이 문서는 **로컬에서 빌드**하여 **Docker Hub**에 이미지를 올리고, **GCP Compute Engine(VM)**에서 받아 실행하기 위한 환경 설정 가이드입니다.

## 1. 프로젝트 및 결제 설정

1.  [Google Cloud Console](https://console.cloud.google.com/)에서 새 프로젝트를 생성합니다 (예: `teacher-management`).
2.  [결제 관리](https://console.cloud.google.com/billing) 페이지에서 결제 계정을 연결합니다.

## 2. Docker Hub 준비

1.  [Docker Hub](https://hub.docker.com/)에 회원가입 및 로그인합니다.
2.  **Create Repository** 버튼을 클릭하여 새 리포지토리를 만듭니다.
    - 이름: `teacher-management-app` (예시)
    - 공개 여부: **Public** (공개) 또는 **Private** (비공개)
        - Private으로 설정 시, 로컬과 서버 모두에서 로그인이 필요합니다.

## 3. Compute Engine (VM) 생성 및 설정

실제 서버가 될 가상 머신을 만듭니다.

1.  **Compute Engine** > **VM 인스턴스** 메뉴로 이동합니다.
2.  **인스턴스 만들기** 클릭:
    - **이름**: `teacher-server`
    - **지역**: `asia-northeast3` (서울)
    - **머신 유형**: `e2-medium` (또는 원하는 사양)
    - **부팅 디스크**: `Ubuntu` (20.04 또는 22.04 LTS 권장)
    - **방화벽**: `HTTP 트래픽 허용`, `HTTPS 트래픽 허용` 체크
3.  **만들기** 클릭.

## 4. 서버(VM)에 Docker 설치

생성된 VM에 SSH로 접속하여 Docker를 설치합니다.

1.  VM 목록에서 **SSH** 버튼을 눌러 접속합니다.
2.  다음 명령어를 차례로 실행합니다 (Docker 설치 및 권한 설정):

```bash
# 패키지 업데이트
sudo apt-get update

# Docker 설치
sudo apt-get install -y docker.io

# 현재 사용자에게 Docker 실행 권한 부여
sudo usermod -aG docker $USER

# 변경 사항 적용을 위해 로그아웃 후 다시 로그인 (또는 창 닫고 다시 SSH 접속)
exit
```

3.  (Private 리포지토리 사용 시) 다시 SSH 접속 후, Docker 로그인:

```bash
docker login
# Username과 Password 입력 (토큰 사용 권장)
```

---

## 5. 배포 스크립트 설정

이제 배포 준비가 끝났습니다.

### 로컬 (내 컴퓨터)
1.  **Docker 로그인**: 터미널에서 `docker login`으로 로그인되어 있어야 합니다.
2.  `scripts/1_build_and_push.sh` 파일을 열어 `DOCKER_USERNAME`과 `REPO_NAME` 변수를 수정합니다.
3.  터미널에서 실행: `./scripts/1_build_and_push.sh`
    - 성공 시 이미지가 Docker Hub에 업로드됩니다.

### 서버 (VM)
1.  `scripts/2_deploy_on_server.sh` 파일을 서버로 복사하거나, 내용을 복사해서 서버에 파일을 만듭니다.
    - 예: `nano deploy.sh` 입력 -> 붙여넣기 -> `Ctrl+O`, `Enter`, `Ctrl+X` 저장.
    - 권한 부여: `chmod +x deploy.sh`
2.  파일 내의 `DOCKER_USERNAME`과 `REPO_NAME` 변수를 수정합니다.
3.  실행: `./deploy.sh`
    - 성공 시 컨테이너가 실행됩니다.

## 6. 방화벽 설정 (외부 접속 허용)

Spring Boot 기본 포트(8080)를 열어줘야 외부에서 접속 가능합니다.
(배포 스크립트에서 `-p 80:8080`을 쓰면 80번 포트를 열면 됩니다.)

1.  **VPC 네트워크** > **방화벽** 이동.
2.  **방화벽 규칙 만들기** 클릭.
3.  이름: `allow-8080`
4.  대상: `네트워크의 모든 인스턴스`
5.  소스 IPv4 범위: `0.0.0.0/0`
6.  프로토콜 및 포트: `tcp: 8080`
7.  만들기.
