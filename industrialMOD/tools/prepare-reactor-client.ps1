param([string]$TestName='run-test-reactor-client-fresh',[string]$HBMJar)
$ErrorActionPreference='Stop'
$taskProject=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if($TestName -notmatch '^run-test-[a-zA-Z0-9.-]+$'){throw 'Use a simple fresh run-test-* directory name.'}
$taskRun=Join-Path $taskProject $TestName
if(Test-Path -LiteralPath $taskRun){throw 'Use a fresh test directory.'}
if(-not $HBMJar){$HBMJar=Join-Path $taskProject 'run-test-production-1.3.0-hbm-verified/mods/HBM-NTM-.1.0.27_X5808.jar'}
if(-not(Test-Path -LiteralPath $HBMJar)){throw 'Point HBMJar at the installed X5808 JAR.'}
$HBMJar=(Resolve-Path -LiteralPath $HBMJar).Path
$taskTools=Join-Path $taskProject '.toolchain/reactors/dev'
$taskForge=Join-Path $taskProject '.gradle-user/caches/minecraft/net/minecraftforge/forge/1.7.10-10.13.4.1614-1.7.10'
$taskJdk=(Get-ChildItem -Directory (Join-Path $taskProject '.toolchain/jdk8') | Select-Object -First 1).FullName
$taskJars=Get-ChildItem (Join-Path $taskProject '.gradle-user/caches/modules-2/files-2.1') -Recurse -Filter '*.jar' | Where-Object { $_.FullName -notmatch '\\jopt-simple\\4.5\\|\\asm-debug-all\\|\\guava\\17.0\\' } | ForEach-Object { $_.FullName }
$taskClasspath=($taskJars -join ';')+';'+(Join-Path $taskForge 'forge-1.7.10-10.13.4.1614-1.7.10-srg.jar')
$taskOutput=Join-Path $taskTools 'HBM-X5808-dev.jar'
New-Item -ItemType Directory -Path $taskTools -Force | Out-Null
Push-Location $taskTools
try {
 & (Join-Path $taskJdk 'bin/java.exe') -Xmx3G -cp $taskClasspath net.md_5.specialsource.SpecialSource --live --in-jar $HBMJar --out-jar $taskOutput --srg-in (Join-Path $taskForge 'srgs/srg-mcp.srg') --quiet
 if($LASTEXITCODE -ne 0){throw 'HBM development remap failed.'}
} finally {Pop-Location}
$taskMods=Join-Path $taskRun 'mods'
New-Item -ItemType Directory -Path $taskMods -Force | Out-Null
Copy-Item -LiteralPath $taskOutput -Destination $taskMods
foreach($taskName in @('CodeChickenCore-1.7.10-1.0.7.47-dev.jar','NotEnoughItems-1.7.10-1.0.5.120-dev.jar','CodeChickenLib-1.7.10-1.1.3.140-dev.jar')){
 Copy-Item -LiteralPath (Join-Path $taskProject ".toolchain/nei/$taskName") -Destination $taskMods
}
Write-Output "Prepared actual HBM X5808 development client and NEI in $taskRun"
Write-Output "Run tools/build.ps1 with runClient -PtestClient -PreactorClient -PtestRunDir=$TestName"
