$ErrorActionPreference = "Stop"

$env:MYSQL_USERNAME = if ($env:MYSQL_USERNAME) { $env:MYSQL_USERNAME } else { "root" }
$env:MYSQL_PASSWORD = if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { "" }

& "$PSScriptRoot\mvn17.ps1" spring-boot:run
