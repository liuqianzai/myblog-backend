$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$env:JAVA_HOME = "D:\jdk17-java17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Push-Location $projectRoot
try {
    mvn -s ".mvn\local-settings.xml" "-Dmaven.repo.local=$projectRoot\.m2\repository" @args
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
