param([string[]]$Tasks = @('test', 'build'))
$ErrorActionPreference = 'Stop'
$projectPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$taskJdkPath = $env:JAVA_HOME
$localJdks = Join-Path $projectPath '.toolchain\jdk8'
if (Test-Path -LiteralPath $localJdks) {
    $taskJdkPath = (Get-ChildItem -Directory -LiteralPath $localJdks | Select-Object -First 1).FullName
}
if (-not $taskJdkPath -or -not (Test-Path -LiteralPath (Join-Path $taskJdkPath 'bin\javac.exe'))) {
    throw 'A Java 8 JDK is required. Set JAVA_HOME to a Java 8 JDK (a JRE is insufficient).'
}
$javaVersion = & (Join-Path $taskJdkPath 'bin\java.exe') -version 2>&1
if (($javaVersion | Out-String) -notmatch 'version "1\.8\.') { throw 'Use a Java 8 JDK for Minecraft Forge 1.7.10.' }
$env:JAVA_HOME = $taskJdkPath
$env:GRADLE_USER_HOME = Join-Path $projectPath '.gradle-user'
Push-Location $projectPath
try {
    & .\gradlew.bat @Tasks --console=plain
    if ($LASTEXITCODE -ne 0) { throw "Gradle exited with code $LASTEXITCODE" }
} finally { Pop-Location }
