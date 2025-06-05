#!/bin/bash
set -euo pipefail

############################################
# 1) 설정 섹션: 환경에 맞게 수정하세요
############################################

# SSH 접속 정보
SSH_USER="ec2-user"
SSH_HOST="54.80.148.39"
SSH_KEY_PATH="/Users/songseungju/Downloads/inha-pj-09.pem"

# 애플리케이션 정보 (실제 프로젝트 이름으로 변경하세요)
APP_NAME="diary_back" # <--- 실제 애플리케이션 이름으로 변경
LOCAL_BUILD_CMD="./gradlew clean bootJar"
LOCAL_JAR_PATH=""

# 원격 배포 디렉터리 및 파일
REMOTE_APP_DIR="/home/ec2-user/app"
REMOTE_JAR_FILENAME="${APP_NAME}.jar"
REMOTE_JAR_PATH="${REMOTE_APP_DIR}/${REMOTE_JAR_FILENAME}"
REMOTE_NOHUP_LOG="${REMOTE_APP_DIR}/nohup.log"
REMOTE_DEPLOY_LOG="${REMOTE_APP_DIR}/deploy.log"

# Java 옵션 (application.yml이 하나뿐이므로 profile 설정 제거)
# 메모리 설정은 EC2 인스턴스 크기에 맞게 조정하세요
JAVA_OPTS="-Xms256m -Xmx512m -Dserver.port=8080"

############################################
# 0) 사전 검증
############################################
echo "[VALIDATION] 배포 전 검증 시작..."

# PEM 키 파일 권한 확인 및 수정
if [ -f "${SSH_KEY_PATH}" ]; then
    CURRENT_PERM=$(stat -f "%A" "${SSH_KEY_PATH}" 2>/dev/null || stat -c "%a" "${SSH_KEY_PATH}" 2>/dev/null)
    if [ "${CURRENT_PERM}" != "400" ] && [ "${CURRENT_PERM}" != "600" ]; then
        echo "[VALIDATION] PEM 키 파일 권한 수정: ${SSH_KEY_PATH}"
        chmod 400 "${SSH_KEY_PATH}"
    fi
else
    echo "[ERROR] PEM 키 파일을 찾을 수 없습니다: ${SSH_KEY_PATH}"
    exit 1
fi

# SSH 연결 테스트
echo "[VALIDATION] SSH 연결 테스트..."
if ! ssh -i "${SSH_KEY_PATH}" -o ConnectTimeout=10 -o BatchMode=yes "${SSH_USER}@${SSH_HOST}" "echo 'SSH 연결 성공'"; then
    echo "[ERROR] SSH 연결 실패. 호스트, 사용자명, 키 파일을 확인하세요."
    exit 1
fi

# Java가 원격 서버에 설치되어 있는지 확인
echo "[VALIDATION] 원격 서버 Java 설치 확인..."
REMOTE_JAVA_VERSION=$(ssh -i "${SSH_KEY_PATH}" "${SSH_USER}@${SSH_HOST}" "java -version 2>&1 | head -n1" || echo "NOT_FOUND")
if [[ "${REMOTE_JAVA_VERSION}" == "NOT_FOUND" ]]; then
    echo "[ERROR] 원격 서버에 Java가 설치되어 있지 않습니다."
    echo "[INFO] 다음 명령어로 Java를 설치하세요:"
    echo "sudo yum update -y && sudo yum install -y java-17-amazon-corretto"
    exit 1
else
    echo "[VALIDATION] 원격 서버 Java 확인: ${REMOTE_JAVA_VERSION}"
fi

############################################
# 2) 로컬에서 JAR 빌드
############################################
echo "[BUILD] 로컬에서 ${APP_NAME} JAR 빌드 시작..."

# Gradle wrapper 실행 권한 확인
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
fi

# 빌드 실행
${LOCAL_BUILD_CMD}

# 빌드된 JAR 파일 경로 찾기
if [[ "${LOCAL_BUILD_CMD}" == *"gradlew"* ]]; then
    LOCAL_JAR_PATH=$(find build/libs -maxdepth 1 -type f -name "*.jar" ! -name "*-plain.jar" -print0 | xargs -0 ls -t | head -n1)
elif [[ "${LOCAL_BUILD_CMD}" == *"mvnw"* || "${LOCAL_BUILD_CMD}" == *"maven"* ]]; then
    LOCAL_JAR_PATH=$(find target -maxdepth 1 -type f -name "*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" -print0 | xargs -0 ls -t | head -n1)
else
    echo "[ERROR] 알 수 없는 빌드 명령어입니다: ${LOCAL_BUILD_CMD}"
    exit 1
fi

if [ -z "${LOCAL_JAR_PATH}" ] || [ ! -f "${LOCAL_JAR_PATH}" ]; then
    echo "[ERROR] 빌드 후 JAR 파일을 찾을 수 없습니다."
    echo "[DEBUG] build/libs 디렉터리 내용:"
    ls -la build/libs/ || echo "build/libs 디렉터리가 존재하지 않습니다."
    exit 1
fi

JAR_SIZE=$(du -h "${LOCAL_JAR_PATH}" | cut -f1)
echo "[BUILD] 빌드 완료: ${LOCAL_JAR_PATH} (크기: ${JAR_SIZE})"

############################################
# 3) 원격 서버에 배포
############################################
echo "[DEPLOY] 원격 서버 배포 시작: ${SSH_USER}@${SSH_HOST}"

# 3-0) 원격 디렉터리 준비
echo "[DEPLOY] 원격 서버 디렉터리 준비..."
ssh -i "${SSH_KEY_PATH}" "${SSH_USER}@${SSH_HOST}" bash -s << EOSSH_PREPARE
set -euo pipefail
mkdir -p "${REMOTE_APP_DIR}"
touch "${REMOTE_DEPLOY_LOG}" "${REMOTE_NOHUP_LOG}"
EOSSH_PREPARE

# 3-1) 기존 프로세스 종료 및 이전 JAR 삭제
echo "[DEPLOY] 기존 애플리케이션 프로세스 종료..."
ssh -i "${SSH_KEY_PATH}" "${SSH_USER}@${SSH_HOST}" bash -s -- "${REMOTE_DEPLOY_LOG}" "${REMOTE_JAR_FILENAME}" "${REMOTE_JAR_PATH}" << 'EOSSH_CLEANUP'
set -euo pipefail

TIME=$(date '+%Y-%m-%d %H:%M:%S')
LOG_FILE="$1"
APP_JAR_FILENAME="$2"
APP_JAR_PATH="$3"

echo "[$TIME] 배포 시작 - 기존 프로세스 정리" >> "$LOG_FILE"

# 더 정확한 프로세스 찾기 (포트 8080을 사용하는 Java 프로세스)
PID=$(lsof -ti:8080 2>/dev/null || true)

if [ -n "$PID" ]; then
    echo "[$TIME] 포트 8080을 사용하는 프로세스 (PID: $PID) 종료 시도..." >> "$LOG_FILE"
    for p in $PID; do
        if ps -p "$p" > /dev/null 2>&1; then
            echo "[$TIME] PID=$p 프로세스 종료 중..." >> "$LOG_FILE"
            kill -15 "$p"
            # 15초 대기
            for i in {1..15}; do
                if ! ps -p "$p" > /dev/null 2>&1; then
                    echo "[$TIME] PID=$p 프로세스 성공적으로 종료됨." >> "$LOG_FILE"
                    break
                fi
                sleep 1
            done
            # 강제 종료 시도
            if ps -p "$p" > /dev/null 2>&1; then
                echo "[$TIME] PID=$p 강제 종료 시도..." >> "$LOG_FILE"
                kill -9 "$p" 2>/dev/null || true
                sleep 2
            fi
        fi
    done
else
    echo "[$TIME] 포트 8080을 사용하는 프로세스 없음." >> "$LOG_FILE"
fi

# 이전 JAR 파일 삭제
if [ -f "${APP_JAR_PATH}" ]; then
    echo "[$TIME] 이전 JAR 파일 삭제: ${APP_JAR_PATH}" >> "$LOG_FILE"
    rm -f "${APP_JAR_PATH}"
fi
EOSSH_CLEANUP

# 3-2) 새 JAR 전송
echo "[DEPLOY] JAR 파일 전송 중... (${JAR_SIZE})"
if ! scp -i "${SSH_KEY_PATH}" "${LOCAL_JAR_PATH}" "${SSH_USER}@${SSH_HOST}:${REMOTE_JAR_PATH}"; then
    echo "[ERROR] JAR 파일 전송 실패."
    exit 1
fi
echo "[DEPLOY] JAR 파일 전송 완료."

# 3-3) 새 애플리케이션 시작
echo "[DEPLOY] 새 애플리케이션 시작..."
ssh -i "${SSH_KEY_PATH}" "${SSH_USER}@${SSH_HOST}" bash -s -- "${REMOTE_DEPLOY_LOG}" "${REMOTE_NOHUP_LOG}" "${REMOTE_JAR_PATH}" "${JAVA_OPTS}" << 'EOSSH_START'
set -euo pipefail

TIME=$(date '+%Y-%m-%d %H:%M:%S')
LOG_FILE="$1"
NOHUP_LOG_FILE="$2"
APP_JAR_PATH="$3"
APP_JAVA_OPTS="$4"

APP_DIR=$(dirname "$APP_JAR_PATH")
cd "$APP_DIR"

echo "[$TIME] 새 애플리케이션 시작: java -jar ${APP_JAVA_OPTS} ${APP_JAR_PATH}" >> "$LOG_FILE"

# 로그 파일 초기화
> "${NOHUP_LOG_FILE}"

# 애플리케이션 시작
nohup java -jar ${APP_JAVA_OPTS} "${APP_JAR_PATH}" > "${NOHUP_LOG_FILE}" 2>&1 &

# 시작 확인 (최대 30초 대기)
echo "[$TIME] 애플리케이션 시작 대기 중..." >> "$LOG_FILE"
for i in {1..30}; do
    sleep 1
    if lsof -ti:8080 > /dev/null 2>&1; then
        NEW_PID=$(lsof -ti:8080)
        echo "[$TIME] 애플리케이션 성공적으로 시작됨. PID: $NEW_PID, 포트: 8080" >> "$LOG_FILE"
        exit 0
    fi
done

echo "[$TIME] 애플리케이션 시작 실패 또는 시간 초과. nohup 로그를 확인하세요." >> "$LOG_FILE"
exit 1
EOSSH_START

if [ $? -eq 0 ]; then
    echo "[DEPLOY] 애플리케이션 시작 성공!"
else
    echo "[ERROR] 애플리케이션 시작 실패. 로그를 확인하세요:"
    echo "ssh -i ${SSH_KEY_PATH} ${SSH_USER}@${SSH_HOST} 'tail -50 ${REMOTE_NOHUP_LOG}'"
    exit 1
fi

############################################
# 4) 배포 후 확인
############################################
echo "[VERIFY] 배포 후 상태 확인..."

# 애플리케이션 상태 확인
ssh -i "${SSH_KEY_PATH}" "${SSH_USER}@${SSH_HOST}" bash -s << 'EOSSH_VERIFY'
echo "=== 애플리케이션 프로세스 상태 ==="
ps aux | grep java | grep -v grep || echo "Java 프로세스 없음"

echo -e "\n=== 포트 8080 상태 ==="
lsof -ti:8080 > /dev/null 2>&1 && echo "포트 8080: 사용 중" || echo "포트 8080: 사용 안함"

echo -e "\n=== 최근 로그 (마지막 10줄) ==="
tail -10 /home/ec2-user/app/nohup.log 2>/dev/null || echo "로그 파일을 읽을 수 없음"
EOSSH_VERIFY

echo ""
echo "[SUCCESS] 배포 완료!"
echo "애플리케이션 URL: http://${SSH_HOST}:8080"
echo "Nginx를 통한 접근: http://${SSH_HOST}/api/"
echo ""
echo "로그 확인 명령어:"
echo "ssh -i ${SSH_KEY_PATH} ${SSH_USER}@${SSH_HOST} 'tail -f ${REMOTE_NOHUP_LOG}'"