param([string]$TestName='run-test-production-1.5.0-hbm-verified',[switch]$WithHBM,[int]$Port=25579,[string]$ReleaseJar,[switch]$DisableIdlePolicy)
$ErrorActionPreference='Stop'
$projectPath=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if($TestName -notmatch '^run-test-[a-zA-Z0-9.-]+$'){throw 'Use a simple run-test-* directory name.'}
$testPath=Join-Path $projectPath $TestName
if((Test-Path (Join-Path $testPath 'integration-world')) -or (Test-Path (Join-Path $testPath 'integration-phase1.done'))){throw 'Use a fresh disposable test folder.'}
$buildText=Get-Content (Join-Path $projectPath 'build.gradle') -Raw
if($buildText -notmatch "(?m)^version = '([^']+)'"){throw 'Project version missing.'}
$testVersion=$Matches[1]
$harnessSource=Join-Path $projectPath "run-test-production-1.5.0-hbm-verified/mods/industrialtech-test-harness-$testVersion.jar"
if(-not(Test-Path -LiteralPath $harnessSource)){throw 'First build with testModJar and reobf (or build), -PproductionTest and -PtestRunDir=run-test-production-1.5.0-hbm-verified.'}
New-Item -ItemType Directory -Path (Join-Path $testPath 'mods'),(Join-Path $testPath 'config') -Force | Out-Null
$harnessDestination=Join-Path $testPath "mods/industrialtech-test-harness-$testVersion.jar"
if([IO.Path]::GetFullPath($harnessSource) -ne [IO.Path]::GetFullPath($harnessDestination)){Copy-Item -LiteralPath $harnessSource -Destination $harnessDestination -Force}
if(-not $ReleaseJar){$ReleaseJar=Join-Path $projectPath "build/libs/IndustrialTechnology-1.7.10-$testVersion.jar"}
Copy-Item -LiteralPath $ReleaseJar -Destination (Join-Path $testPath 'mods') -Force
if($WithHBM){Copy-Item -LiteralPath (Join-Path $projectPath 'run-test-production-1.3.0-hbm-verified/mods/HBM-NTM-.1.0.27_X5808.jar') -Destination (Join-Path $testPath 'mods') -Force}
if($DisableIdlePolicy){@'
performance {
 B:pauseOwnedChunkLoadingInUnoccupiedDimensions=false
}
'@ | Set-Content -LiteralPath (Join-Path $testPath 'config/industrialtech-space.cfg')}
'eula=true' | Set-Content -LiteralPath (Join-Path $testPath 'eula.txt')
@"
level-name=integration-world
level-type=FLAT
generator-settings=2;7;1;
server-ip=127.0.0.1
server-port=$Port
online-mode=false
spawn-protection=0
spawn-animals=false
spawn-monsters=false
spawn-npcs=false
max-players=4
allow-nether=false
view-distance=3
snooper-enabled=false
"@ | Set-Content -LiteralPath (Join-Path $testPath 'server.properties')
@'
defaults {
 B:enabled=true
 I:maximumChunksPerTicket=8
 I:maximumTicketCount=8
}
industrialtech {
 I:maximumChunksPerTicket=8
 I:maximumTicketCount=8
}
'@ | Set-Content -LiteralPath (Join-Path $testPath 'config/forgeChunkLoading.cfg')
$taskJdkPath=(Get-ChildItem -Directory (Join-Path $projectPath '.toolchain/jdk8') | Select-Object -First 1).FullName
$runtimeJars=(Get-Content (Join-Path $projectPath '.toolchain/runtime-classpath.txt') -Raw).Trim()
$testClasspath=(Join-Path $projectPath '.toolchain/forge-universal.jar')+';'+(Join-Path $projectPath '.gradle-user/caches/minecraft/net/minecraft/minecraft_server/1.7.10/minecraft_server-1.7.10.jar')+';'+$runtimeJars
Push-Location $testPath
try {
    for($pass=0;$pass -lt 2;$pass++){
        $phase=if($pass -eq 0){'initial'}else{'restart'}
        $logPath=Join-Path $projectPath ".toolchain/$TestName-$phase.log"
        & (Join-Path $taskJdkPath 'bin/java.exe') -Xms512M -Xmx4G -cp $testClasspath net.minecraft.launchwrapper.Launch --tweakClass cpw.mods.fml.common.launcher.FMLServerTweaker nogui *> $logPath
        if($LASTEXITCODE -ne 0){Get-Content -LiteralPath $logPath -Tail 80;throw 'Ordinary Forge server failed.'}
        $report=Get-Content -LiteralPath "integration-$phase.txt";$report
        if($report -match '^FAIL:' -or $report[-1] -ne 'Failed: 0'){throw 'World integration checks failed.'}
    }
} finally {Pop-Location}
Write-Output "Reports: $testPath"
