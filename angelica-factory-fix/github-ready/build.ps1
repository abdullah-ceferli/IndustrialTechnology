# SPDX-License-Identifier: LGPL-3.0-only
[CmdletBinding()]
param(
    [string]$JdkHome = $env:JAVA_HOME,
    [string]$ForgeDevJar,
    [string]$DependencyDirectory = (Join-Path $PSScriptRoot '.deps'),
    [switch]$Offline
)
. (Join-Path $PSScriptRoot 'hbm-patch\common.ps1')

$jdk = Get-PatchJdk -JdkHome $JdkHome
if ([string]::IsNullOrWhiteSpace($ForgeDevJar) -or !(Test-Path -LiteralPath $ForgeDevJar -PathType Leaf)) {
    throw 'Pass -ForgeDevJar with an MCP-named Forge 1.7.10/10.13.4.1614 development JAR. See BUILDING.md.'
}
$ForgeDevJar = (Resolve-Path -LiteralPath $ForgeDevJar).Path
$dependencies = Get-PatchDependencies -Repository $PSScriptRoot -Directory $DependencyDirectory -Offline:$Offline
$original = $dependencies['angelica-2.2.36.jar']
$asm = $dependencies['asm-debug-all-5.0.3.jar']
$fastutil = $dependencies['fastutil-8.5.18.jar']
$classes = Join-Path $PSScriptRoot 'build\hbm-overlay\classes'
$tools = Join-Path $PSScriptRoot 'build\hbm-overlay\tools'
New-Item -ItemType Directory -Path $classes, $tools -Force | Out-Null
$sources = @(
    (Join-Path $PSScriptRoot 'src\main\java\com\gtnewhorizons\angelica\rendering\TileEntityRenderBoundsRegistry.java'),
    (Join-Path $PSScriptRoot 'src\main\java\com\gtnewhorizons\angelica\rendering\HbmFactoryRenderFallback.java')
)
& $jdk.Javac -source 8 -target 8 -encoding UTF-8 -cp "$ForgeDevJar;$original;$fastutil" -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Helper compilation failed. Check that ForgeDevJar uses MCP development names.' }
& $jdk.Javac -source 8 -target 8 -encoding UTF-8 -cp $asm -d $tools (Join-Path $PSScriptRoot 'hbm-patch\tools\OverlayClass.java')
if ($LASTEXITCODE -ne 0) { throw 'Overlay tool compilation failed.' }
$output = Join-Path $PSScriptRoot 'build\angelica-2.2.36-personal-hbm3.jar'
& $jdk.Java -cp "$tools;$asm" OverlayClass $original $classes $output
if ($LASTEXITCODE -ne 0) { throw 'Overlay failed.' }
& (Join-Path $PSScriptRoot 'test.ps1') -JdkHome $JdkHome -DependencyDirectory $DependencyDirectory -Offline
Get-FileHash -LiteralPath $output -Algorithm SHA256
