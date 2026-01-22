@echo off
echo Running Codename One Tests...
mvn clean test-compile com.codenameone:codenameone-maven-plugin:test -Dtrue -f javase/pom.xml
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ----------------------------------------
    echo TESTS PASSED
    echo ----------------------------------------
) else (
    echo.
    echo ----------------------------------------
    echo TESTS FAILED
    echo ----------------------------------------
)
pause
