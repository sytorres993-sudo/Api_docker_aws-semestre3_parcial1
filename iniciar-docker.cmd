@echo off
setlocal
pushd "%~dp0"
if not exist ".env" (
    echo Falta .env. Copia .env.example a .env y configura DB_PASSWORD antes de iniciar.
    popd
    exit /b 1
)
docker compose up -d --build --wait --wait-timeout 240
set "gimnasioExit=%ERRORLEVEL%"
if "%gimnasioExit%"=="0" (
    echo.
    echo Gimnasio Sara disponible en http://localhost:8080
    echo Si cambiaste APP_PORT en .env, usa ese puerto en la URL.
) else (
    echo No se pudo iniciar. Revisa Docker Desktop y ejecuta: docker compose logs --tail 60 app db
)
popd
exit /b %gimnasioExit%
