@echo off
echo =====================================
echo Iniciando API na porta padrao (8090)
echo =====================================

echo Starting keepalive loop...
start "Keepalive" cmd /k "_keepalive.bat"

@REM mvnw.cmd spring-boot:run
@REM if %ERRORLEVEL% NEQ 0 (
@REM     echo.
@REM     echo Porta 8090 ocupada ou falha ao iniciar.
@REM     echo Tentando porta alternativa 8091...
@REM     echo.
@REM     call mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8091
@REM )
call mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8091

pause
