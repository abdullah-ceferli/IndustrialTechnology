param([string]$TestName='run-test-consoles-2.6.0',[int]$Port=25631,[switch]$WithPipes,[switch]$WithHBM,[string]$ReleaseJar)
$ErrorActionPreference='Stop'
$projectPath=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if($TestName -notmatch '^run-test-[a-zA-Z0-9.-]+$'){throw 'Use a fresh run-test-* directory name.'}
$testPath=Join-Path $projectPath $TestName
if(Test-Path -LiteralPath $testPath){throw 'Use a fresh disposable test directory.'}
New-Item -ItemType Directory -Path (Join-Path $testPath 'mods') -Force | Out-Null
$versionText=Get-Content (Join-Path $projectPath 'build.gradle') -Raw
if($versionText -notmatch "(?m)^version = '([^']+)'"){throw 'Version missing'}
$version=$Matches[1]
if(-not $ReleaseJar){$ReleaseJar=Join-Path $projectPath "build/libs/IndustrialTechnology-1.7.10-$version.jar"}
Copy-Item -LiteralPath $ReleaseJar -Destination (Join-Path $testPath 'mods')
Copy-Item -LiteralPath (Join-Path $projectPath "run-test-production-1.5.0-hbm-verified/mods/industrialtech-test-harness-$version.jar") -Destination (Join-Path $testPath 'mods')
$gameMods='D:\programs\Maincraft\Minecraft\game\mods'
if($WithHBM){Copy-Item -LiteralPath (Join-Path $gameMods 'HBM-NTM-.1.0.27_X5771.jar') -Destination (Join-Path $testPath 'mods')}
if($WithPipes){foreach($name in @('CoFHCore-[1.7.10]3.1.4-329.jar','ThermalFoundation-[1.7.10]1.2.6-118.jar','ThermalExpansion-[1.7.10]4.1.5-248.jar','ThermalDynamics-[1.7.10]1.2.1-172.jar','appliedenergistics2-rv3-beta-6.jar')){Copy-Item -LiteralPath (Join-Path $gameMods $name) -Destination (Join-Path $testPath 'mods')}}
'eula=true' | Set-Content -LiteralPath (Join-Path $testPath 'eula.txt')
@"
level-name=console-world
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
$taskJava=Join-Path $projectPath '.toolchain/jdk8/jdk8u504-b01/bin/java.exe'
$testClasspath=(Join-Path $projectPath '.toolchain/forge-universal.jar')+';'+(Join-Path $projectPath '.gradle-user/caches/minecraft/net/minecraft/minecraft_server/1.7.10/minecraft_server-1.7.10.jar')+';'+(Get-Content (Join-Path $projectPath '.toolchain/runtime-classpath.txt') -Raw).Trim()
Push-Location $testPath
try{foreach($phase in @('initial','restart')){
    & $taskJava -Xms512M -Xmx4G '-Dindustrialtech.consoleTest=true' -cp $testClasspath net.minecraft.launchwrapper.Launch --tweakClass cpw.mods.fml.common.launcher.FMLServerTweaker nogui *> (Join-Path $projectPath ".toolchain/$TestName-$phase.log")
    if($LASTEXITCODE -ne 0){Get-Content (Join-Path $projectPath ".toolchain/$TestName-$phase.log") -Tail 80;throw 'Native console server failed.'}
    $report=Get-Content "console-$phase.txt";$report
    if($report -match '^FAIL:' -or $report[-1] -ne 'Failed: 0'){throw 'Console checks failed.'}
}}finally{Pop-Location}
