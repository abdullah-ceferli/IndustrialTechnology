param([string]$TestName='run-test-batteries-client-2.8.0-a')
$ErrorActionPreference='Stop'
$projectPath=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if($TestName -notmatch '^run-test-[a-zA-Z0-9.-]+$'){throw 'Use a fresh disposable run-test directory.'}
$testPath=Join-Path $projectPath $TestName
if(Test-Path -LiteralPath $testPath){throw 'Test directory already exists.'}
New-Item -ItemType Directory -Path (Join-Path $testPath 'mods') | Out-Null
New-Item -ItemType Directory -Path (Join-Path $testPath 'mods/1.7.10') | Out-Null
Copy-Item -LiteralPath (Join-Path $projectPath '.toolchain/nei/CodeChickenLib-1.7.10-1.1.3.140-dev.jar') -Destination (Join-Path $testPath 'mods/1.7.10')
Copy-Item -LiteralPath (Join-Path $projectPath '.toolchain/hbm-api/HBM-X5771-dev.jar') -Destination (Join-Path $testPath 'mods')
foreach($name in @('CodeChickenCore-1.7.10-1.0.7.47-dev.jar','NotEnoughItems-1.7.10-1.0.5.120-dev.jar')){Copy-Item -LiteralPath (Join-Path $projectPath ".toolchain/nei/$name") -Destination (Join-Path $testPath 'mods')}
& (Join-Path $PSScriptRoot 'build.ps1') -Tasks @('runClient','-PtestClient','-PbatteryClient',"-PtestRunDir=$TestName") *> (Join-Path $projectPath ".toolchain/$TestName.log")
if($LASTEXITCODE -ne 0){Get-Content (Join-Path $projectPath ".toolchain/$TestName.log") -Tail 60;throw 'Battery client failed.'}
Get-Content (Join-Path $testPath 'battery-client-report.txt')
