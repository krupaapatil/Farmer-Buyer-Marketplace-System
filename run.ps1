param(
    [ValidateSet("web", "desktop")]
    [string]$Mode = "web",
    [int]$Port = 8080,
    [switch]$OpenBrowser
)

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

function Get-JavaToolPath {
    param([string]$toolName)

    $command = Get-Command $toolName -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    $localJdk = Get-ChildItem ".tools\jdk" -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($localJdk) {
        $candidate = Join-Path $localJdk.FullName "bin\$toolName"
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    throw "Could not find $toolName. Install JDK 17+ or place a JDK under .tools\jdk."
}

$javac = Get-JavaToolPath "javac.exe"
$java = Get-JavaToolPath "java.exe"

New-Item -ItemType Directory -Force -Path out | Out-Null
$sourceFiles = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }
$runtimeClasspath = "out;lib\*"

Write-Host "Compiling project..."
& $javac --add-modules jdk.httpserver -d out $sourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "Compilation failed."
}

if ($Mode -eq "desktop") {
    Write-Host "Launching Swing desktop app..."
    & $java -cp $runtimeClasspath farmmarket.app.FarmerBuyerMarketplaceApp
    exit $LASTEXITCODE
}

$localIp = (Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
    Where-Object { $_.IPAddress -notlike "127.*" -and $_.PrefixOrigin -ne "WellKnown" } |
    Select-Object -First 1 -ExpandProperty IPAddress)

if (-not $localIp) {
    $localIp = "localhost"
}

$localUrl = "http://localhost:$Port"
$networkUrl = "http://${localIp}:$Port"

Write-Host "Launching web app..."
Write-Host "Open locally: $localUrl"
Write-Host "Open on the same Wi-Fi/network: $networkUrl"

if ($OpenBrowser) {
    Start-Process $localUrl
}

& $java --add-modules jdk.httpserver -cp $runtimeClasspath farmmarket.web.MarketplaceWebServer $Port
