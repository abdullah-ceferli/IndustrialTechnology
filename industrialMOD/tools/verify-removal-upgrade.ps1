param([string]$TestName='run-test-production-2.2.0-upgrade',[string]$ReleaseJar,[string]$HarnessJar,[int]$Port=25663)
$ErrorActionPreference='Stop'
$removalRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if($TestName -notmatch '^run-test-[a-zA-Z0-9.-]+$'){throw 'Use a fresh disposable test name'}
$removalRun=Join-Path $removalRoot $TestName
if(Test-Path -LiteralPath $removalRun){throw 'Use a fresh folder; no world will be deleted'}
$removalSource=Join-Path $removalRoot 'run-test-space-2.1.0-accepted-standalone'
$removalHashes=Get-ChildItem -LiteralPath "$removalSource/space-world" -Recurse -File | ForEach-Object {[pscustomobject]@{Path=$_.FullName;SHA256=(Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash}}
New-Item -ItemType Directory -Path $removalRun,"$removalRun/mods" | Out-Null
foreach($removalPart in @('space-world','config','server.properties','eula.txt')){Copy-Item -LiteralPath (Join-Path $removalSource $removalPart) -Destination $removalRun -Recurse}
if(-not $ReleaseJar){$ReleaseJar="$removalRoot/build/libs/IndustrialTechnology-1.7.10-2.2.0.jar"}
if(-not $HarnessJar){$HarnessJar="$removalRoot/run-test-production-1.5.0-hbm-verified/mods/industrialtech-test-harness-2.2.0.jar"}
Copy-Item -LiteralPath $ReleaseJar,$HarnessJar -Destination "$removalRun/mods"
(Get-Content -LiteralPath "$removalRun/server.properties") -replace '^server-port=.*',"server-port=$Port" | Set-Content -LiteralPath "$removalRun/server.properties"
$removalCp="$removalRoot/.toolchain/forge-universal.jar;$removalRoot/.gradle-user/caches/minecraft/net/minecraft/minecraft_server/1.7.10/minecraft_server-1.7.10.jar;"+(Get-Content -LiteralPath "$removalRoot/.toolchain/runtime-classpath.txt" -Raw).Trim()
Push-Location $removalRun
try {& "$removalRoot/.toolchain/jdk8/jdk8u504-b01/bin/java.exe" '-Dindustrialtech.removalUpgrade=true' -Xms512M -Xmx3G -cp $removalCp net.minecraft.launchwrapper.Launch --tweakClass cpw.mods.fml.common.launcher.FMLServerTweaker nogui *> "$removalRoot/.toolchain/$TestName.log"
 if($LASTEXITCODE -ne 0){Get-Content -LiteralPath "$removalRoot/.toolchain/$TestName.log" -Tail 70;throw 'Upgrade failed'}
 $removalReport=Get-Content -LiteralPath 'removal-upgrade.txt';$removalReport
 if($removalReport[-1] -ne 'Failed: 0' -or $removalReport -match '^FAIL:'){throw 'Upgrade assertions failed'}
}finally{Pop-Location}
foreach($removalHash in $removalHashes){if((Get-FileHash -LiteralPath $removalHash.Path -Algorithm SHA256).Hash -ne $removalHash.SHA256){throw 'Original test save changed'}}
$removalHashes | ConvertTo-Json | Set-Content -LiteralPath "$removalRun/unchanged-source-save-sha256.json"
Write-Output 'Original 2.1.0 disposable save is byte-for-byte unchanged.'
