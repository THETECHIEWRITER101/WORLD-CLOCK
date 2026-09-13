@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

set "JAVA_EXE="
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

for /d %%D in ("%ProgramFiles%\Java\*" "%ProgramFiles%\Eclipse Adoptium\*" "%ProgramFiles%\JetBrains\IntelliJ IDEA*\jbr") do (
  if not defined JAVA_EXE (
    if exist "%%~D\bin\java.exe" set "JAVA_EXE=%%~D\bin\java.exe"
  )
)

if not defined JAVA_EXE (
  if exist "%ProgramFiles%\JetBrains\IntelliJ IDEA 2026.1.1\jbr\bin\java.exe" set "JAVA_EXE=%ProgramFiles%\JetBrains\IntelliJ IDEA 2026.1.1\jbr\bin\java.exe"
)

if not defined JAVA_EXE set "JAVA_EXE=java"

"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
exit /b %ERRORLEVEL%
