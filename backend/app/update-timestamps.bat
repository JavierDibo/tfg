@echo off
REM Batch script to update timestamp placeholders with actual file modification times
REM Usage: update-timestamps.bat

echo Updating timestamp placeholders in files...

REM Run PowerShell script with execution policy bypass
powershell -ExecutionPolicy Bypass -File "%~dp0update-timestamps.ps1"

echo Timestamp update completed.
