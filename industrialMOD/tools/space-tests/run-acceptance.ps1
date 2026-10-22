$ErrorActionPreference='Stop'
$spaceRoot=[IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$spaceRelease=Join-Path $spaceRoot '.toolchain/space-release/IndustrialTechnology-1.7.10-2.0.0.jar'
$spaceHarness=Join-Path $spaceRoot '.toolchain/space-release/industrialtech-space-checks-1.jar'
Push-Location $spaceRoot
try{
 & "$PSScriptRoot/run-server.ps1" -TestName run-test-space-2.0.0-release-standalone -Port 25625 -ReleaseJar $spaceRelease -HarnessJar $spaceHarness
 & "$PSScriptRoot/run-server.ps1" -TestName run-test-space-2.0.0-release-hbm5771 -Port 25626 -HbmPath 'D:/programs/Maincraft/Minecraft/game/mods/HBM-NTM-.1.0.27_X5771.jar' -ReleaseJar $spaceRelease -HarnessJar $spaceHarness
 & "$PSScriptRoot/run-server.ps1" -TestName run-test-space-2.0.0-release-hbm5808 -Port 25628 -HbmPath 'run-test-production-1.3.0-hbm-verified/mods/HBM-NTM-.1.0.27_X5808.jar' -ReleaseJar $spaceRelease -HarnessJar $spaceHarness
 & "$PSScriptRoot/run-server.ps1" -TestName run-test-space-2.0.0-release-conflict -Port 25630 -Conflict -ReleaseJar $spaceRelease -HarnessJar $spaceHarness
 & "$spaceRoot/tools/verify-wireless-production.ps1" -TestName run-test-production-2.0.0-release-standalone -Port 25627 -DisableIdlePolicy -ReleaseJar $spaceRelease
 & "$spaceRoot/tools/verify-wireless-production.ps1" -TestName run-test-production-2.0.0-release-hbm -WithHBM -Port 25629 -DisableIdlePolicy -ReleaseJar $spaceRelease
}finally{Pop-Location}
