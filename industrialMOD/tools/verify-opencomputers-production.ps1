param([string]$Name='combined',[switch]$WithPack,[switch]$Reference,[switch]$Restart)
$ErrorActionPreference='Stop'
$taskRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if($Name -notmatch '^[a-zA-Z0-9.-]+$'){throw 'Invalid test name.'}
$taskRun=Join-Path $taskRoot ".toolchain/oc-tests/$Name"
if(Test-Path -LiteralPath $taskRun){throw 'Use a fresh disposable test directory.'}
New-Item -ItemType Directory -Path (Join-Path $taskRun 'mods') -Force | Out-Null
if($Reference){
    Copy-Item -LiteralPath (Join-Path $taskRoot '.toolchain/battery-monitor-release-2.8.0/IndustrialTechnology-1.7.10-2.8.0.jar') -Destination (Join-Path $taskRun 'mods')
    Copy-Item -LiteralPath (Join-Path $taskRoot 'vendor/opencomputers/OpenComputers-MC1.7.10-1.8.10+667626d-universal.jar') -Destination (Join-Path $taskRun 'mods')
}else{Copy-Item -LiteralPath (Join-Path $taskRoot 'build/libs/IndustrialTechnology-1.7.10-2.9.0.jar') -Destination (Join-Path $taskRun 'mods')}
Copy-Item -LiteralPath (Join-Path $taskRoot '.toolchain/oc-tests/build-run/mods/industrialtech-test-harness-2.9.0.jar') -Destination (Join-Path $taskRun 'mods')
if($WithPack){
    $taskGameMods='D:\programs\Maincraft\Minecraft\game\mods'
    foreach($taskMod in @('HBM-NTM-.1.0.27_X5771.jar','CoFHCore-[1.7.10]3.1.4-329.jar','ThermalFoundation-[1.7.10]1.2.6-118.jar','ThermalExpansion-[1.7.10]4.1.5-248.jar','ThermalDynamics-[1.7.10]1.2.1-172.jar','appliedenergistics2-rv3-beta-6.jar','ProjectE-1.7.10-PE1.10.1.jar')){Copy-Item -LiteralPath (Join-Path $taskGameMods $taskMod) -Destination (Join-Path $taskRun 'mods')}
}
'eula=true' | Set-Content -LiteralPath (Join-Path $taskRun 'eula.txt')
@'
level-name=oc-test-world
level-type=FLAT
generator-settings=2;7;1;
server-ip=127.0.0.1
server-port=25651
online-mode=false
spawn-protection=0
spawn-animals=false
spawn-monsters=false
spawn-npcs=false
max-players=4
allow-nether=false
view-distance=3
snooper-enabled=false
'@ | Set-Content -LiteralPath (Join-Path $taskRun 'server.properties')
$taskJava=Join-Path $taskRoot '.toolchain/jdk8/jdk8u504-b01/bin/java.exe'
$taskClasspath=(Join-Path $taskRoot '.toolchain/forge-universal.jar')+';'+(Join-Path $taskRoot '.gradle-user/caches/minecraft/net/minecraft/minecraft_server/1.7.10/minecraft_server-1.7.10.jar')+';'+(Get-Content -LiteralPath (Join-Path $taskRoot '.toolchain/runtime-classpath.txt') -Raw).Trim()
$taskProperties=Join-Path $taskRun 'server.properties'
if($Reference){[IO.File]::WriteAllText($taskProperties,([IO.File]::ReadAllText($taskProperties).Replace('server-port=25651','server-port=25652')))}
elseif($WithPack){[IO.File]::WriteAllText($taskProperties,([IO.File]::ReadAllText($taskProperties).Replace('server-port=25651','server-port=25653')))}
Push-Location $taskRun
try{
    $taskPhases=@('initial');if($Restart){$taskPhases+=@('restart')}
    foreach($taskPhase in $taskPhases){
        & $taskJava -Xms512M -Xmx4G '-Dindustrialtech.ocTest=true' -cp $taskClasspath net.minecraft.launchwrapper.Launch --tweakClass cpw.mods.fml.common.launcher.FMLServerTweaker nogui *> (Join-Path $taskRoot ".toolchain/oc-tests/$Name-$taskPhase.log")
        if($LASTEXITCODE -ne 0){Get-Content -LiteralPath (Join-Path $taskRoot ".toolchain/oc-tests/$Name-$taskPhase.log") -Tail 55;throw 'OC native server failed.'}
        $taskReport=Get-Content -LiteralPath (Join-Path $taskRun "oc-$taskPhase.txt");$taskReport
        if($taskReport[-1] -ne 'Failed: 0' -or $taskReport -match '^FAIL:'){throw 'OC native checks failed.'}
    }
}finally{Pop-Location}
