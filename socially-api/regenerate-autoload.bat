@echo off
REM Composer autoload regeneration script
REM Run this after adding new classes

echo Regenerating Composer autoload...
echo.

REM Try composer command
where composer >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    composer dump-autoload
    goto :success
)

REM Try php composer.phar
if exist composer.phar (
    C:\xampp\php\php.exe composer.phar dump-autoload
    goto :success
)

echo ERROR: Could not find composer. Please ensure:
echo 1. Composer is installed globally, OR
echo 2. composer.phar exists in this directory, AND
echo 3. PHP is available at C:\xampp\php\php.exe
echo.
pause
exit /b 1

:success
echo.
echo Autoload regenerated successfully!
echo.
pause
