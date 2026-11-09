@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Start-Friends-Service.ps1" -BindAddress 0.0.0.0
pause
