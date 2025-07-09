@echo off
REM Batch script to run all .jpl files in the examples directory using the JPL CLI

setlocal enabledelayedexpansion

set JPL_JAR=..\target\JPL-1.0-SNAPSHOT.jar
set EXAMPLES_DIR=.

for %%f in (%EXAMPLES_DIR%\*.jpl) do (
    echo Running %%f
    java -jar %JPL_JAR% run %%f
    echo --------------------------------------
)

echo All example tests completed.

