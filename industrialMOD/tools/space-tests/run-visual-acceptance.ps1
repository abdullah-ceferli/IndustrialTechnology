$ErrorActionPreference='Stop'
$visualRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$visualRelease=Join-Path $visualRoot '.toolchain/space-visual-release-final/IndustrialTechnology-1.7.10-2.0.1.jar'
$visualHarness=Join-Path $visualRoot '.toolchain/space-visual-release-final/industrialtech-space-checks-1.jar'
Push-Location $visualRoot
try {
    & "$PSScriptRoot/run-server.ps1" -TestName run-test-space-2.0.1-accepted-standalone -Port 25640 -ReleaseJar $visualRelease -HarnessJar $visualHarness
    & "$PSScriptRoot/run-server.ps1" -TestName run-test-space-2.0.1-accepted-hbm5771 -Port 25641 -HbmPath 'D:/programs/Maincraft/Minecraft/game/mods/HBM-NTM-.1.0.27_X5771.jar' -ReleaseJar $visualRelease -HarnessJar $visualHarness
    & "$PSScriptRoot/run-visual-upgrade.ps1" -TestName run-test-space-2.0.1-accepted-upgrade -ReleaseJar $visualRelease -HarnessJar $visualHarness
} finally {Pop-Location}
