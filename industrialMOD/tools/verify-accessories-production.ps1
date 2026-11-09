param([string]$Name='standalone-a',[switch]$WithOriginals,[switch]$WithProjectE,[switch]$WithHbm,[switch]$Restart,[string]$RuntimeJar='')
$ErrorActionPreference='Stop';$taskRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if($Name -notmatch '^[a-zA-Z0-9.-]+$'){throw 'Invalid disposable test name.'}
$taskRun=Join-Path $taskRoot ".toolchain/accessory-tests/$Name";if(Test-Path -LiteralPath $taskRun){throw 'Use a fresh test directory.'};New-Item -ItemType Directory -Path (Join-Path $taskRun 'mods') -Force | Out-Null
$taskGame='D:\programs\Maincraft\Minecraft\game\mods'
if(-not $RuntimeJar){$RuntimeJar=Join-Path $taskRoot 'build/libs/IndustrialTechnology-1.7.10-2.10.1.jar'}
Copy-Item -LiteralPath $RuntimeJar -Destination (Join-Path $taskRun 'mods')
Copy-Item -LiteralPath (Join-Path $taskRoot '.toolchain/accessory-tests/build-run/mods/industrialtech-test-harness-2.10.1.jar') -Destination (Join-Path $taskRun 'mods')
Copy-Item -LiteralPath (Join-Path $taskGame 'Baubles-1.7.10-1.0.1.10.jar') -Destination (Join-Path $taskRun 'mods')
if($WithOriginals -or $WithProjectE){Copy-Item -LiteralPath (Join-Path $taskGame 'ProjectE-1.7.10-PE1.10.1.jar') -Destination (Join-Path $taskRun 'mods')}
if($WithOriginals){Copy-Item -LiteralPath (Join-Path $taskRoot '.toolchain/accessory-tests/coexist-final-b/mods/baublelicious-1.7.10-1.2.2-final.jar') -Destination (Join-Path $taskRun 'mods')}
if($WithHbm){Copy-Item -LiteralPath (Join-Path $taskGame 'HBM-NTM-.1.0.27_X5771.jar') -Destination (Join-Path $taskRun 'mods')}
'eula=true' | Set-Content -LiteralPath (Join-Path $taskRun 'eula.txt')
$taskPort=25671;if($WithOriginals){$taskPort=25672}elseif($WithProjectE){$taskPort=25673}
@"
level-name=accessory-test-world
level-type=FLAT
generator-settings=2;7;1;
server-ip=127.0.0.1
server-port=$taskPort
online-mode=false
spawn-protection=0
spawn-animals=false
spawn-monsters=false
spawn-npcs=false
max-players=4
allow-nether=false
view-distance=3
snooper-enabled=false
"@ | Set-Content -LiteralPath (Join-Path $taskRun 'server.properties')
$taskJava=Join-Path $taskRoot '.toolchain/jdk8/jdk8u504-b01/bin/java.exe';$taskCp=(Join-Path $taskRoot '.toolchain/forge-universal.jar')+';'+(Join-Path $taskRoot '.gradle-user/caches/minecraft/net/minecraft/minecraft_server/1.7.10/minecraft_server-1.7.10.jar')+';'+(Get-Content -LiteralPath (Join-Path $taskRoot '.toolchain/runtime-classpath.txt') -Raw).Trim()
Push-Location $taskRun
try{$taskPhases=@('initial');if($Restart){$taskPhases+=@('restart')};foreach($taskPhase in $taskPhases){& $taskJava -Xms512M -Xmx4G '-Dindustrialtech.accessoryTest=true' -cp $taskCp net.minecraft.launchwrapper.Launch --tweakClass cpw.mods.fml.common.launcher.FMLServerTweaker nogui *> (Join-Path $taskRoot ".toolchain/accessory-tests/$Name-$taskPhase.log");if($LASTEXITCODE -ne 0){Get-Content -LiteralPath (Join-Path $taskRoot ".toolchain/accessory-tests/$Name-$taskPhase.log") -Tail 40;throw 'Accessory server failed.'};$taskReport=Get-Content -LiteralPath (Join-Path $taskRun "accessories-$taskPhase.txt");$taskReport;if($taskReport[-1] -ne 'Failed: 0'){throw 'Accessory native checks failed.'}}}finally{Pop-Location}
