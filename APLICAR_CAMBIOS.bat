@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0APLICAR_CAMBIOS.ps1"
echo.
pause
