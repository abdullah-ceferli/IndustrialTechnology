param([string]$TestName='run-test-space-2.1.0-upgrade',[string]$ReleaseJar,[string]$HarnessJar)
$ErrorActionPreference='Stop'
$visualRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
if($TestName -notmatch '^run-test-space-[a-zA-Z0-9.-]+$'){throw 'Use a fresh run-test-space-* name.'}
$visualRun=Join-Path $visualRoot $TestName
if(Test-Path -LiteralPath $visualRun){throw 'Use a fresh folder; no world is removed.'}
$visualSource=Join-Path $visualRoot 'run-test-space-2.0.1-accepted-standalone'
if(-not(Test-Path -LiteralPath (Join-Path $visualSource 'space-world/level.dat'))){throw 'Original disposable 2.0.1 test save missing.'}
New-Item -ItemType Directory -Path $visualRun,"$visualRun/mods" | Out-Null
$visualHashes=Get-ChildItem -LiteralPath "$visualSource/space-world" -Recurse -File | ForEach-Object { [pscustomobject]@{Path=$_.FullName;SHA256=(Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash} }
foreach($visualFile in @('space-world','config','eula.txt','server.properties','space-phase1.done')){Copy-Item -LiteralPath (Join-Path $visualSource $visualFile) -Destination $visualRun -Recurse}
Copy-Item -LiteralPath $ReleaseJar,$HarnessJar -Destination "$visualRun/mods"
(Get-Content -LiteralPath "$visualRun/server.properties") -replace '^server-port=.*','server-port=25652' | Set-Content -LiteralPath "$visualRun/server.properties"
$visualJava=Join-Path $visualRoot '.toolchain/jdk8/jdk8u504-b01/bin/java.exe'
$visualCp="$visualRoot/.toolchain/forge-universal.jar;$visualRoot/.gradle-user/caches/minecraft/net/minecraft/minecraft_server/1.7.10/minecraft_server-1.7.10.jar;"+(Get-Content -LiteralPath "$visualRoot/.toolchain/runtime-classpath.txt" -Raw).Trim()
Push-Location $visualRun
try{& $visualJava "-Dindustrialtech.shuttleUpgrade=true" -Xms512M -Xmx3G -cp $visualCp net.minecraft.launchwrapper.Launch --tweakClass cpw.mods.fml.common.launcher.FMLServerTweaker nogui *> "$visualRoot/.toolchain/$TestName.log"
 if($LASTEXITCODE -ne 0){throw 'Upgrade server failed.'}
 $visualReport=Get-Content -LiteralPath 'space-report.txt';$visualReport
 if($visualReport[-1] -ne 'Failed: 0' -or $visualReport -match '^FAIL:'){throw 'Upgrade checks failed.'}
 Copy-Item -LiteralPath 'space-report.txt' -Destination 'upgrade-report.txt'
}finally{Pop-Location}
foreach($visualEntry in $visualHashes){if((Get-FileHash -LiteralPath $visualEntry.Path -Algorithm SHA256).Hash -ne $visualEntry.SHA256){throw 'Source disposable save was changed.'}}
$visualHashes | ConvertTo-Json | Set-Content -LiteralPath "$visualRun/unchanged-source-save-sha256.json"
Write-Output 'Original 2.0.1 disposable save hashes unchanged; copy loaded successfully.'
