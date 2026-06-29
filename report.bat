@echo off
setlocal

REM ============================================================
REM  Allure: отчёт с накоплением истории и трендов
REM  Использование:
REM    report.bat                          - отчёт по последним результатам
REM    report.bat test                     - прогнать тесты, затем отчёт
REM    report.bat test -P regression -Denv=preprod
REM ============================================================

set RESULTS=target\allure-results
set REPORT=target\allure-report
set HISTORY=allure-history

REM --- 1. Опциональный прогон тестов ---
if /I "%1"=="test" (
    shift
    echo [1/5] Running tests...
    call mvn clean test %2 %3 %4 %5 %6 %7 %8 %9
)

if not exist "%RESULTS%" (
    echo ERROR: %RESULTS% не найден. Сначала прогони тесты:  report.bat test
    exit /b 1
)

REM --- 2. Подсеять историю прошлых прогонов ---
if exist "%HISTORY%" (
    echo [2/5] Seeding history...
    if not exist "%RESULTS%\history" mkdir "%RESULTS%\history"
    xcopy /E /I /Y "%HISTORY%\*" "%RESULTS%\history\" >nul
)

REM --- 3. Метка прогона (executor.json) для подписей трендов ---
echo [3/5] Stamping build...
for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmm"') do set BUILD=run-%%i
> "%RESULTS%\executor.json" echo {
>>"%RESULTS%\executor.json" echo   "name": "Local",
>>"%RESULTS%\executor.json" echo   "type": "manual",
>>"%RESULTS%\executor.json" echo   "buildName": "%BUILD%"
>>"%RESULTS%\executor.json" echo }

REM --- 4. Сгенерировать отчёт ---
echo [4/5] Generating report...
call allure generate "%RESULTS%" --clean -o "%REPORT%"

REM --- 5. Сохранить историю обратно (для следующего прогона) ---
if exist "%REPORT%\history" (
    echo [5/5] Saving history...
    if not exist "%HISTORY%" mkdir "%HISTORY%"
    xcopy /E /I /Y "%REPORT%\history\*" "%HISTORY%\" >nul
)

echo.
echo Done. Opening report...
call allure open "%REPORT%"

endlocal