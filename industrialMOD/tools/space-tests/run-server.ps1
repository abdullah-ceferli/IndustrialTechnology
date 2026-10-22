param([string]$TestName='run-test-space-standalone-1',[string]$HbmPath,[int]$Port=25625,[switch]$Conflict,[string]$ReleaseJar,[string]$HarnessJar)
$ErrorActionPreference='Stop'
$spaceProject=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
if($TestName -notmatch '^run-test-space-[a-zA-Z0-9.-]+$'){throw 'Use a new run-test-space-* name.'}
$spaceRun=Join-Path $spaceProject $TestName
if(Test-Path -LiteralPath $spaceRun){throw 'Test folder already exists; use a fresh name. No world is deleted.'}
New-Item -ItemType Directory -Path "$spaceRun/mods","$spaceRun/config" | Out-Null
$spaceBuild=Get-Content -LiteralPath "$spaceProject/build.gradle" -Raw
$spaceVersion=[regex]::Match($spaceBuild,"(?m)^version = '([^']+)'").Groups[1].Value
if(-not $ReleaseJar){$ReleaseJar="$spaceProject/build/libs/IndustrialTechnology-1.7.10-$spaceVersion.jar"}
Copy-Item -LiteralPath $ReleaseJar -Destination "$spaceRun/mods"
if(-not $HarnessJar){$HarnessJar="$spaceProject/.toolchain/space-checks/industrialtech-space-checks-1.jar"}
Copy-Item -LiteralPath $HarnessJar -Destination "$spaceRun/mods"
if($HbmPath){Copy-Item -LiteralPath $HbmPath -Destination "$spaceRun/mods"}
'eula=true' | Set-Content -LiteralPath "$spaceRun/eula.txt"
@"
level-name=space-world
level-type=FLAT
generator-settings=2;7;1;
server-ip=127.0.0.1
server-port=$Port
online-mode=false
spawn-protection=0
spawn-animals=false
spawn-monsters=false
spawn-npcs=false
allow-nether=false
view-distance=3
snooper-enabled=false
"@ | Set-Content -LiteralPath "$spaceRun/server.properties"
$spaceJava=Join-Path $spaceProject '.toolchain/jdk8/jdk8u504-b01/bin/java.exe'
$spaceCp="$spaceProject/.toolchain/forge-universal.jar;$spaceProject/.gradle-user/caches/minecraft/net/minecraft/minecraft_server/1.7.10/minecraft_server-1.7.10.jar;"+(Get-Content -LiteralPath "$spaceProject/.toolchain/runtime-classpath.txt" -Raw).Trim()
Push-Location $spaceRun
try{foreach($spacePhase in @('initial','restart')){
    $spaceLog="$spaceProject/.toolchain/$TestName-$spacePhase.log"
    & $spaceJava "-Dindustrialtech.spaceConflict=$($Conflict.IsPresent.ToString().ToLowerInvariant())" -Xms512M -Xmx3G -cp $spaceCp net.minecraft.launchwrapper.Launch --tweakClass cpw.mods.fml.common.launcher.FMLServerTweaker nogui *> $spaceLog
    if($LASTEXITCODE -ne 0 -or !(Test-Path -LiteralPath 'space-report.txt')){Get-Content -LiteralPath $spaceLog -Tail 65;throw 'Disposable Forge server failed.'}
    Copy-Item -LiteralPath 'space-report.txt' -Destination "space-$spacePhase.txt"
    $spaceReport=Get-Content -LiteralPath "space-$spacePhase.txt";$spaceReport
    if($spaceReport -match '^FAIL:' -or $spaceReport[-1] -ne 'Failed: 0'){throw 'Native space checks failed.'}
}}finally{Pop-Location}
