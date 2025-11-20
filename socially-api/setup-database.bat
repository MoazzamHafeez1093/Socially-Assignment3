@echo off
REM Database setup script for Socially API
REM Run this after configuring .env file

echo ====================================
echo Socially API Database Setup
echo ====================================
echo.

REM Read database credentials from .env (basic parsing)
for /f "tokens=1,2 delims==" %%a in (.env) do (
    if "%%a"=="DB_HOST" set DB_HOST=%%b
    if "%%a"=="DB_DATABASE" set DB_DATABASE=%%b
    if "%%a"=="DB_USERNAME" set DB_USERNAME=%%b
    if "%%a"=="DB_PASSWORD" set DB_PASSWORD=%%b
)

echo Database: %DB_DATABASE%
echo Host: %DB_HOST%
echo Username: %DB_USERNAME%
echo.

echo Creating database if not exists...
mysql -h %DB_HOST% -u %DB_USERNAME% -p%DB_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS %DB_DATABASE% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to create database. Check MySQL credentials.
    pause
    exit /b 1
)

echo Importing schema...
mysql -h %DB_HOST% -u %DB_USERNAME% -p%DB_PASSWORD% %DB_DATABASE% < database\migrations\001_create_tables.sql

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to import schema.
    pause
    exit /b 1
)

echo.
echo ====================================
echo Database setup completed successfully!
echo ====================================
echo.
echo You can now access the API at:
echo http://localhost/socially-api/public/health
echo.
pause
