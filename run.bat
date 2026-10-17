@echo off
cd /d "%~dp0"
if not exist out mkdir out
if exist sources.tmp del sources.tmp
for /r src %%f in (*.java) do echo "%%f" >> sources.tmp
javac -encoding UTF-8 -d out @sources.tmp
if errorlevel 1 (
    del sources.tmp
    exit /b 1
)
del sources.tmp
java -cp out Main
