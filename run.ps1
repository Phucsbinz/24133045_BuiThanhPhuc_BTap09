$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

Write-Host "===================================================================" -ForegroundColor Cyan
Write-Host " BTAP09: Spring Security 7 & Spring Boot 4 (VD1 & VD2)" -ForegroundColor Cyan
Write-Host " Sinh vien: Bui Thanh Phuc - MSSV: 24133045" -ForegroundColor Cyan
Write-Host "===================================================================" -ForegroundColor Cyan

# 1. Configure Java 26
$jdkCandidates = @(
    "C:\Program Files\Java\jdk-26.0.2.1",
    "C:\Program Files\Java\latest",
    $env:JAVA_HOME
)

$selectedJdk = $null
foreach ($cand in $jdkCandidates) {
    if ($cand -and (Test-Path "$cand\bin\java.exe")) {
        $selectedJdk = $cand
        break
    }
}

if ($selectedJdk) {
    $env:JAVA_HOME = $selectedJdk
    $env:Path = "$selectedJdk\bin;$env:Path"
    Write-Host "[JDK] Using Java 26 at: $selectedJdk" -ForegroundColor Green
} else {
    Write-Warning "JDK 26 not found in default paths. Using existing PATH java."
}

& java -version

# 2. Environment Variables & MySQL
if (-not $env:DB_USERNAME) { $env:DB_USERNAME = 'root' }
if (-not $env:DB_PASSWORD) { $env:DB_PASSWORD = 'ptpn06082006' }
if (-not $env:SERVER_PORT) { $env:SERVER_PORT = '8091' }

# 3. Check / Initialize MySQL database btap09_vd12
$mysqlExe = (Get-ChildItem "C:\Program Files\MySQL\MySQL Server*\bin\mysql.exe" -ErrorAction SilentlyContinue | Select-Object -First 1).FullName
if ($mysqlExe -and (Test-Path $mysqlExe)) {
    Write-Host "[MySQL] Checking database btap09_vd12..." -ForegroundColor Yellow
    & $mysqlExe -u $env:DB_USERNAME "-p$env:DB_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS btap09_vd12 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    Write-Host "[MySQL] Database btap09_vd12 is ready." -ForegroundColor Green
}

# 4. Build application
$jarFile = "target\24133045_BuiThanhPhuc_Security_VD12-1.0.0.jar"
if (-not (Test-Path $jarFile)) {
    Write-Host "[BUILD] Building project with Maven Wrapper..." -ForegroundColor Yellow
    & .\mvnw.cmd -B clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Maven build failed!"
        exit 1
    }
}

Write-Host ""
Write-Host "===================================================================" -ForegroundColor Green
Write-Host " UNG DUNG SAN SANG CHAY TAI:" -ForegroundColor Green
Write-Host " • Cong chon bai (Portal) : http://localhost:8091/" -ForegroundColor White
Write-Host " • Vi du 1 (VD1 Home)     : http://localhost:8091/vd1/home" -ForegroundColor White
Write-Host " • Vi du 1 (VD1 Login)    : http://localhost:8091/vd1/login" -ForegroundColor White
Write-Host " • Vi du 2 (VD2 Home)     : http://localhost:8091/vd2/home" -ForegroundColor White
Write-Host " • Vi du 2 (VD2 Login)    : http://localhost:8091/vd2/login" -ForegroundColor White
Write-Host "-------------------------------------------------------------------" -ForegroundColor Green
Write-Host " TAI KHOAN KIEM THU (Mat khau: 123456):" -ForegroundColor Yellow
Write-Host " • Admin (Quyen ADMIN)    : admin / trungnh@hcmute.edu.vn" -ForegroundColor White
Write-Host " • User 01 (Co anh)       : user01 / user01@gmail.com" -ForegroundColor White
Write-Host " • User 02 (Khong anh)    : user02 / phuc.bui@example.com" -ForegroundColor White
Write-Host " • Disabled (Bi khoa)     : disabled_user / locked@example.com" -ForegroundColor White
Write-Host "===================================================================" -ForegroundColor Green
Write-Host ""

& java -jar $jarFile
