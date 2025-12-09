@echo off
setlocal

REM ===== 설정 =====
set IMAGE_NAME=teamkwak-rl-ai
set RL_DIR=rl

echo [INFO] Checking Docker image "%IMAGE_NAME%"...

REM 이미지가 없으면 빌드
docker image inspect %IMAGE_NAME% >nul 2>&1
if errorlevel 1 (
    echo [INFO] Docker image not found. Building image...
    docker build -t %IMAGE_NAME% .\%RL_DIR%
    if errorlevel 1 (
        echo [ERROR] Docker build failed.
        pause
        exit /b 1
    )
) else (
    echo [INFO] Docker image already exists.
)

echo [INFO] Starting AI container...
REM 새 콘솔창에서 AI 컨테이너 실행 (게임이랑 동시에 돌기 위함)
start "" cmd /c docker run --rm --name %IMAGE_NAME%-container %IMAGE_NAME%

echo [INFO] Starting Java game...
call gradlew.bat run

echo [INFO] Game finished. Stopping AI container if still running...
docker stop %IMAGE_NAME%-container >nul 2>&1

echo [INFO] Done.
pause
endlocal
