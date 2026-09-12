param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("validate", "migrate", "repair")]
    [string]$Action,

    [string]$DbHost = "localhost",
    [int]$DbPort = 5432,
    [string]$DbName = "lifeadmin-ai",
    [string]$DbUser = "platform",
    [string]$DbPassword = "8BCF-A6CC55D89593",
    [string]$MigrationLocation = "filesystem:src/main/resources/db/migration"
)

$ErrorActionPreference = "Stop"

$jdbcUrl = "jdbc:postgresql://$DbHost`:$DbPort/$DbName"
$goal = "org.flywaydb:flyway-maven-plugin:12.4.0:$Action"

Write-Host "Running Flyway $Action against $jdbcUrl" -ForegroundColor Cyan

if ($Action -eq "repair") {
    Write-Warning "Use repair only when schema is already correct and only Flyway checksum/history has drifted."
}

$cmdArgs = @(
    "-Dflyway.url=$jdbcUrl",
    "-Dflyway.user=$DbUser",
    "-Dflyway.password=$DbPassword",
    "-Dflyway.locations=$MigrationLocation",
    $goal
)

& .\mvnw @cmdArgs

if ($LASTEXITCODE -ne 0) {
    throw "Flyway $Action failed with exit code $LASTEXITCODE"
}

Write-Host "Flyway $Action completed successfully." -ForegroundColor Green
