@echo off
setlocal enabledelayedexpansion

REM ============================================================
REM  Allure: отчёт с накоплением истории и трендов
REM  Использование:
REM    report.bat                          - отчёт по последним результатам
REM    report.bat test                     - прогнать тесты, затем отчёт
REM    report.bat test -P regression -Denv=preprod
REM    report.bat -single                  - + single-file отчёт (для пересылки)
REM    report.bat test -single             - прогнать + оба отчёта
REM ============================================================

set RESULTS=target\allure-results
set REPORT=target\allure-report
set REPORT_SINGLE=target\allure-report-single
set HISTORY=allure-history

REM --- Разбор аргументов: вытащить -single, остальное передать в mvn ---
set SINGLE=0
set MVN_ARGS=

if /I "%1"=="test" (
    shift
    set RUN_TESTS=1
) else (
    set RUN_TESTS=0
)

:parse_args
if "%1"=="" goto args_done
if /I "%1"=="-single" (
    set SINGLE=1
) else (
    set MVN_ARGS=!MVN_ARGS! %1
)
shift
goto parse_args
:args_done

REM --- 1. Опциональный прогон тестов ---
if "%RUN_TESTS%"=="1" (
    echo [1/6] Running tests...
    call mvn clean test %MVN_ARGS%
)

if not exist "%RESULTS%" (
    echo ERROR: %RESULTS% не найден. Сначала прогони тесты:  report.bat test
    exit /b 1
)

REM --- 2. Подсеять историю прошлых прогонов ---
if exist "%HISTORY%" (
    echo [2/6] Seeding history...
    if not exist "%RESULTS%\history" mkdir "%RESULTS%\history"
    xcopy /E /I /Y "%HISTORY%\*" "%RESULTS%\history\" >nul
)

REM --- 3. Метка прогона (executor.json) для подписей трендов ---
echo [3/6] Stamping build...
for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmm"') do set BUILD=run-%%i
> "%RESULTS%\executor.json" echo {
>>"%RESULTS%\executor.json" echo   "name": "Local",
>>"%RESULTS%\executor.json" echo   "type": "manual",
>>"%RESULTS%\executor.json" echo   "buildName": "%BUILD%"
>>"%RESULTS%\executor.json" echo }

REM --- 4. Сгенерировать отчёт ---
echo [4/6] Generating report...
call allure generate "%RESULTS%" --clean -o "%REPORT%"

REM --- 5. Сохранить историю обратно (для следующего прогона) ---
if exist "%REPORT%\history" (
    echo [5/6] Saving history...
    if not exist "%HISTORY%" mkdir "%HISTORY%"
    xcopy /E /I /Y "%REPORT%\history\*" "%HISTORY%\" >nul
)

REM --- 6. Опциональный single-file отчёт (для пересылки) ---
if "%SINGLE%"=="1" (
    echo [6/6] Generating single-file report for sharing...
    call allure generate "%RESULTS%" --single-file --clean -o "%REPORT_SINGLE%"
    echo Single-file report: %REPORT_SINGLE%\index.html
)

echo.
echo Done. Opening report...
call allure open "%REPORT%"

endlocal