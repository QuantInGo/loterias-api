@echo off
echo =====================================
echo Iniciando API na porta padrao (8090)
echo =====================================

call mvnw.cmd spring-boot:run
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Porta 8090 ocupada ou falha ao iniciar.
    echo Tentando porta alternativa 8091...
    echo.
    call mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8091
)

pause
