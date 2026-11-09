param([string]$BindAddress = '127.0.0.1', [int]$Port = 24464, [string]$JavaPath = '')
$ErrorActionPreference = 'Stop'
if ($JavaPath) { $taskJava = $JavaPath }
elseif ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) { $taskJava = Join-Path $env:JAVA_HOME 'bin\java.exe' }
elseif (Test-Path -LiteralPath 'D:\programs\Maincraft\Minecraft\jre\jre-legacy\windows-x64\jre-legacy\bin\java.exe') { $taskJava = 'D:\programs\Maincraft\Minecraft\jre\jre-legacy\windows-x64\jre-legacy\bin\java.exe' }
else { $taskJava = (Get-Command java -ErrorAction Stop).Source }
$taskKeytool = Join-Path (Split-Path $taskJava -Parent) 'keytool.exe'
if (!(Test-Path -LiteralPath $taskKeytool)) { throw 'Java keytool is needed for the first start. Choose a Java 8 installation containing keytool.exe.' }
$jarPath = Join-Path $PSScriptRoot 'OfflineSkins-Friends-Service-1.2.0.jar'
if (!(Test-Path -LiteralPath $jarPath)) { throw 'The friends service JAR is missing.' }
$dataPath = Join-Path $PSScriptRoot 'data'
New-Item -ItemType Directory -Path $dataPath -Force | Out-Null
$passwordPath = Join-Path $dataPath 'service.password'
$storePath = Join-Path $dataPath 'service.jks'
if (!(Test-Path -LiteralPath $passwordPath)) {
    if (Test-Path -LiteralPath $storePath) { throw 'Restore service.password from your service backup; the existing certificate was preserved.' }
    $random = [Security.Cryptography.RandomNumberGenerator]::Create()
    $passwordBytes = New-Object byte[] 32
    $random.GetBytes($passwordBytes)
    $random.Dispose()
    [IO.File]::WriteAllText($passwordPath, [BitConverter]::ToString($passwordBytes).Replace('-','').ToLowerInvariant(), [Text.UTF8Encoding]::new($false))
}
if (!(Test-Path -LiteralPath $storePath)) {
    & $taskKeytool -genkeypair -alias offlineskins -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -validity 3650 -storetype JKS -keystore $storePath '-storepass:file' $passwordPath '-keypass:file' $passwordPath -dname 'CN=Offline Skins Friends Service' -noprompt
    if ($LASTEXITCODE -ne 0) { throw 'Could not generate the friends service certificate.' }
}
Write-Host 'Copy the complete service address (including #fingerprint) into Minecraft: F8 -> Friends -> Connect.'
Write-Host 'Keep this service running while friends use automatic skin sharing.'
Write-Host 'For other PCs use -BindAddress 0.0.0.0 and a reachable address. No firewall or router settings are changed by this script.'
& $taskJava '-Xms32M' '-Xmx128M' '-Dsun.net.httpserver.maxReqTime=15' '-Dsun.net.httpserver.maxRspTime=15' -jar $jarPath --bind $BindAddress --port $Port --data $dataPath --keystore $storePath --password-file $passwordPath
if ($LASTEXITCODE -ne 0) { throw "Friends service exited with code $LASTEXITCODE" }
