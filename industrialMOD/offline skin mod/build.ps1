param([string[]]$Tasks = @('test', 'build', 'sourcesJar'))
$ErrorActionPreference = 'Stop'
$parent = Split-Path $PSScriptRoot -Parent
$taskJdk = Join-Path $parent '.toolchain\jdk8\jdk8u504-b01'
if (!(Test-Path -LiteralPath (Join-Path $taskJdk 'bin\javac.exe'))) {
    $taskJdk = $env:JAVA_HOME
}
if (!$taskJdk -or !(Test-Path -LiteralPath (Join-Path $taskJdk 'bin\javac.exe'))) {
    throw 'Set JAVA_HOME to a Java 8 JDK.'
}
$env:JAVA_HOME = $taskJdk
$cacheArgs = @()
if (Test-Path -LiteralPath (Join-Path $parent '.gradle-user')) {
    $env:GRADLE_USER_HOME = Join-Path $parent '.gradle-user'
    $cacheArgs = @('--offline')
}
& (Join-Path $PSScriptRoot 'gradlew.bat') -p $PSScriptRoot @Tasks @cacheArgs --console=plain
if ($LASTEXITCODE -ne 0) { throw "Build failed with code $LASTEXITCODE" }
