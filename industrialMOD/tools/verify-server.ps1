# Creates its own new disposable world, accepts the Minecraft EULA for that test server,
# runs world tests, restarts for persisted-ticket tests, and leaves reports for inspection.
$ErrorActionPreference = 'Stop'
$projectPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$testName = 'run-test-' + (Get-Date -Format 'yyyyMMdd-HHmmss')
$testPath = Join-Path $projectPath $testName
if (Test-Path -LiteralPath $testPath) { throw 'Test directory already exists; retry with a fresh timestamp.' }
New-Item -ItemType Directory -Path (Join-Path $testPath 'config') -Force | Out-Null
'eula=true' | Set-Content -LiteralPath (Join-Path $testPath 'eula.txt')
@'
level-name=integration-world
level-type=FLAT
generator-settings=2;7;1;
server-ip=127.0.0.1
server-port=25579
online-mode=false
spawn-protection=0
spawn-animals=false
spawn-monsters=false
spawn-npcs=false
max-players=4
allow-nether=false
view-distance=3
snooper-enabled=false
'@ | Set-Content -LiteralPath (Join-Path $testPath 'server.properties')
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
'@ | Set-Content -LiteralPath (Join-Path $testPath 'config\forgeChunkLoading.cfg')
for ($pass = 0; $pass -lt 2; $pass++) {
    & (Join-Path $PSScriptRoot 'build.ps1') -Tasks @('runServer', '-PtestServer', "-PtestRunDir=$testName")
    $reportName = if ($pass -eq 0) { 'integration-initial.txt' } else { 'integration-restart.txt' }
    $reportPath = Join-Path $testPath $reportName
    if (-not (Test-Path -LiteralPath $reportPath)) { throw "Test report missing: $reportPath" }
    $report = Get-Content -LiteralPath $reportPath
    $report
    if ($report -match '^FAIL:' -or $report[-1] -ne 'Failed: 0') { throw 'Dedicated server tests failed.' }
}
Write-Output "Reports: $testPath"
