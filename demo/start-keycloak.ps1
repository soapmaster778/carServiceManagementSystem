$ErrorActionPreference = "Stop"

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$keycloakDir = Join-Path $projectDir ".runtime\keycloak-26.3.3"
$realmSource = Join-Path $projectDir "keycloak\car-service-realm.json"
$realmTarget = Join-Path $keycloakDir "data\import\car-service-realm.json"
$bundledJava = "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\jbr"

if (-not (Test-Path (Join-Path $keycloakDir "bin\kc.bat"))) {
    throw "Keycloak runtime is missing: $keycloakDir"
}

New-Item -ItemType Directory -Path (Split-Path $realmTarget) -Force | Out-Null
Copy-Item -LiteralPath $realmSource -Destination $realmTarget -Force

if (Test-Path (Join-Path $bundledJava "bin\java.exe")) {
    $env:JAVA_HOME = $bundledJava
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

$env:KC_BOOTSTRAP_ADMIN_USERNAME = "admin"
$env:KC_BOOTSTRAP_ADMIN_PASSWORD = "admin"

Write-Host "Starting Keycloak at http://localhost:8082" -ForegroundColor Green
& (Join-Path $keycloakDir "bin\kc.bat") start-dev --http-port=8082 --import-realm
