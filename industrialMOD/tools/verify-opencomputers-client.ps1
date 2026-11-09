param([string]$Name='client-a')
$ErrorActionPreference='Stop'
$taskRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if($Name -notmatch '^client-[a-zA-Z0-9.-]+$'){throw 'Invalid disposable client name.'}
$taskRelative=".toolchain/oc-tests/$Name";$taskPath=Join-Path $taskRoot $taskRelative
if(Test-Path -LiteralPath $taskPath){throw 'Use a fresh disposable test folder.'}
New-Item -ItemType Directory -Path (Join-Path $taskPath 'mods/1.7.10') -Force | Out-Null
Copy-Item -LiteralPath (Join-Path $taskRoot '.toolchain/hbm-api/HBM-X5771-dev.jar') -Destination (Join-Path $taskPath 'mods')
Copy-Item -LiteralPath (Join-Path $taskRoot '.toolchain/nei/CodeChickenLib-1.7.10-1.1.3.140-dev.jar') -Destination (Join-Path $taskPath 'mods/1.7.10')
foreach($taskName in @('CodeChickenCore-1.7.10-1.0.7.47-dev.jar','NotEnoughItems-1.7.10-1.0.5.120-dev.jar')){Copy-Item -LiteralPath (Join-Path $taskRoot ".toolchain/nei/$taskName") -Destination (Join-Path $taskPath 'mods')}
& (Join-Path $PSScriptRoot 'build.ps1') -Tasks @('runClient','-PtestClient','-PocClient',"-PtestRunDir=$taskRelative") *> (Join-Path $taskRoot ".toolchain/oc-tests/$Name.log")
if($LASTEXITCODE -ne 0){Get-Content -LiteralPath (Join-Path $taskRoot ".toolchain/oc-tests/$Name.log") -Tail 60;throw 'OC graphical test launch failed.'}
Get-Content -LiteralPath (Join-Path $taskPath 'oc-client.txt')
if((Get-Content -LiteralPath (Join-Path $taskPath 'oc-client.txt'))[-1] -ne 'Failed: 0'){throw 'OC graphical checks failed.'}
