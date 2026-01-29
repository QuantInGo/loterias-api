@echo off
title API Keepalive Timer

set INTERVAL=480

:loop
cls
set LAST_DATE=%date%
set LAST_TIME=%time%

echo =========================================
echo   API KEEPALIVE - Loterias API
echo =========================================
echo.
echo [%LAST_DATE% %LAST_TIME%] Sending keepalive ping...
curl -s https://loterias-api-sspn.onrender.com/api/megasena > nul
echo Ping sent successfully.
echo.

set /a remaining=%INTERVAL%

:countdown
cls
echo =========================================
echo   API KEEPALIVE - Loterias API
echo =========================================
echo.
echo Last ping:
echo   %LAST_DATE% %LAST_TIME%
echo.
echo Next ping in:
echo.
echo   %remaining% seconds
echo.
echo (Press CTRL+C to stop)
timeout /t 1 /nobreak > nul
set /a remaining-=1
if %remaining% GTR 0 goto countdown

goto loop
