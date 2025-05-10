@echo off
REM Load environment variables from .env file
for /f "tokens=*" %%a in (.env) do (
    echo %%a | findstr /v "^#" > nul && set %%a
)
REM Start the application
mvn spring-boot:run
