$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$jdkCandidates = @('C:\Program Files\Java\jdk-26.0.2.1', $env:JAVA_HOME)
$selectedJdk = $jdkCandidates | Where-Object { $_ -and (Test-Path (Join-Path $_ 'bin\java.exe')) } | Select-Object -First 1
if ($selectedJdk) {
    $env:JAVA_HOME = $selectedJdk
    $env:Path = "$selectedJdk\bin;$env:Path"
}

if (-not (Test-Path '.env')) {
    throw 'Missing .env. Copy .env.example to .env and fill in local MySQL, Gmail App Password, and Cloudinary settings.'
}
$configuredKeys = @{}
foreach ($line in Get-Content '.env') {
    if ($line -match '^\s*([^#=\s]+)\s*=(.*)$') {
        $configuredKeys[$Matches[1]] = $Matches[2]
    }
}
foreach ($key in @('DB_USERNAME', 'DB_PASSWORD')) {
    if (-not $configuredKeys.ContainsKey($key) -or [string]::IsNullOrWhiteSpace($configuredKeys[$key])) {
        throw "Set $key in .env before starting the application."
    }
}

$jarFile = 'target\24133045_BuiThanhPhuc_Security_VD12-1.0.0.jar'
if (-not (Test-Path $jarFile)) {
    & .\mvnw.cmd -B -ntp package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw 'Maven build failed.' }
}

Write-Host 'Starting BTAP09 VD1, VD2, and VD3 on the configured port.' -ForegroundColor Green
Write-Host 'Portal: check SERVER_PORT in .env (default 8091), path /'
Write-Host 'VD1:    /vd1/login'
Write-Host 'VD2:    /vd2/login'
Write-Host 'VD3:    /vd3/login'
& java -jar $jarFile
