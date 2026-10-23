# SPDX-License-Identifier: LGPL-3.0-only
[CmdletBinding()]
param(
    [string]$JdkHome = $env:JAVA_HOME,
    [string]$DependencyDirectory = (Join-Path $PSScriptRoot '.deps'),
    [switch]$Offline
)
. (Join-Path $PSScriptRoot 'hbm-patch\common.ps1')

$jdk = Get-PatchJdk -JdkHome $JdkHome
$dependencies = Get-PatchDependencies -Repository $PSScriptRoot -Directory $DependencyDirectory -Offline:$Offline
$original = $dependencies['angelica-2.2.36.jar']
$asm = $dependencies['asm-debug-all-5.0.3.jar']
$fastutil = $dependencies['fastutil-8.5.18.jar']
$jar = Join-Path $PSScriptRoot 'build\angelica-2.2.36-personal-hbm3.jar'
if (!(Test-Path -LiteralPath $jar -PathType Leaf)) { throw 'Build the overlay first with build.ps1.' }
$classes = Join-Path $PSScriptRoot 'build\hbm-overlay\test-classes'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$sources = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'hbm-patch\tests') -Recurse -Filter '*.java' | ForEach-Object FullName)
& $jdk.Javac -source 8 -target 8 -encoding UTF-8 -cp "$jar;$asm" -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
& $jdk.Java -Xverify:all -cp "$classes;$jar;$fastutil;$asm" VerifyOverlay $original $jar
if ($LASTEXITCODE -ne 0) { throw 'Overlay verification failed.' }
