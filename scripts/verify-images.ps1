$ErrorActionPreference = "Stop"

$env:GOOGLE_CLIENT_ID = "dummy"
$env:GOOGLE_CLIENT_SECRET = "dummy"
$env:KAKAO_CLIENT_ID = "dummy"
$env:KAKAO_CLIENT_SECRET = "dummy"
$env:OAUTH2_SUCCESS_REDIRECT_URI = "http://localhost:3000/success"
$env:OAUTH2_FAILURE_REDIRECT_URI = "http://localhost:3000/failure"
$env:JWT_SECRET = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz"
$env:JWT_ACCESS_TOKEN_EXPIRATION_SECONDS = "3600"

$root = Split-Path -Path $PSScriptRoot -Parent
$logOut = Join-Path $root "build/bootrun-local.log"
$logErr = Join-Path $root "build/bootrun-local.err.log"

if (Test-Path $logOut) { Remove-Item $logOut -Force }
if (Test-Path $logErr) { Remove-Item $logErr -Force }

$process = Start-Process `
    -FilePath (Join-Path $root "gradlew.bat") `
    -WorkingDirectory $root `
    -ArgumentList ":core:core-api:bootRun", "-Dspring-boot.run.arguments=--spring.profiles.active=local,--server.port=18080" `
    -PassThru `
    -RedirectStandardOutput $logOut `
    -RedirectStandardError $logErr

try {
    $ready = $false
    $apiResponse = $null

    for ($i = 0; $i -lt 120; $i++) {
        Start-Sleep -Seconds 1
        try {
            $apiResponse = Invoke-WebRequest -Uri "http://localhost:18080/api/v1/shows/latest?category=CONCERT" -UseBasicParsing -TimeoutSec 2
            if ($apiResponse.StatusCode -eq 200) {
                $ready = $true
                break
            }
        } catch {}
    }

    if (-not $ready) {
        throw "Server did not start in time. See $logOut and $logErr."
    }

    $json = $apiResponse.Content | ConvertFrom-Json
    $imagePath = $json.data.shows[0].image
    $imageResponse = Invoke-WebRequest -Uri ("http://localhost:18080" + $imagePath) -UseBasicParsing -TimeoutSec 5

    [PSCustomObject]@{
        ApiStatus = $apiResponse.StatusCode
        SampleImagePath = $imagePath
        ImageStatus = $imageResponse.StatusCode
        ImageContentType = $imageResponse.Headers["Content-Type"]
    } | ConvertTo-Json -Depth 3
}
finally {
    if ($process -and -not $process.HasExited) {
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
    }
}
