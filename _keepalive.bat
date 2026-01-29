@echo off
title API Keepalive

:loop
echo [%date% %time%] Keepalive ping...
curl -s https://loterias-api-sspn.onrender.com/api/megasena > nul
timeout /t 480 /nobreak > nul
goto loop
