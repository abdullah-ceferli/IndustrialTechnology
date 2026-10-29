$ErrorActionPreference='Stop'
$fpsBackupRoot=[IO.Path]::GetFullPath($PSScriptRoot)
$fpsManifest=Get-Content -LiteralPath (Join-Path $fpsBackupRoot 'manifest.json') -Raw | ConvertFrom-Json
$fpsMainMods=[IO.Path]::GetFullPath('D:\programs\Maincraft\Minecraft\game\mods')
$fpsRunning=Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" | Where-Object {$_.CommandLine -match '--gameDir\s+"?D:\\programs\\Maincraft\\Minecraft\\game"?(?:\s|$)'}
if($fpsRunning){throw 'Close Minecraft before restoring the renderer.'}
foreach($fpsEntry in $fpsManifest.new){
 $fpsTarget=[IO.Path]::GetFullPath($fpsEntry.path)
 if(-not $fpsTarget.StartsWith($fpsMainMods+'\',[StringComparison]::OrdinalIgnoreCase)){throw 'Invalid mod target'}
 if(-not(Test-Path -LiteralPath $fpsTarget) -or (Get-FileHash -LiteralPath $fpsTarget -Algorithm SHA256).Hash -ne $fpsEntry.sha256){throw 'A restored JAR changed; stop to preserve it'}
}
foreach($fpsEntry in $fpsManifest.old){
 $fpsTarget=[IO.Path]::GetFullPath($fpsEntry.path);$fpsSource=[IO.Path]::GetFullPath($fpsEntry.backup)
 if(-not $fpsTarget.StartsWith($fpsMainMods+'\',[StringComparison]::OrdinalIgnoreCase) -or -not $fpsSource.StartsWith($fpsBackupRoot+'\',[StringComparison]::OrdinalIgnoreCase)){throw 'Invalid backup path'}
 if((Test-Path -LiteralPath $fpsTarget) -or (Get-FileHash -LiteralPath $fpsSource -Algorithm SHA256).Hash -ne $fpsEntry.sha256){throw 'Original backup or active mod changed'}
}
$fpsDisabled=Join-Path $fpsBackupRoot 'disabled-restoration'
if(Test-Path -LiteralPath $fpsDisabled){throw 'Rollback already performed'}
New-Item -ItemType Directory -Path $fpsDisabled | Out-Null
foreach($fpsEntry in $fpsManifest.new){Move-Item -LiteralPath $fpsEntry.path -Destination (Join-Path $fpsDisabled ([IO.Path]::GetFileName($fpsEntry.path)))}
foreach($fpsEntry in $fpsManifest.old){Move-Item -LiteralPath $fpsEntry.backup -Destination $fpsEntry.path}
Write-Output 'Original renderer libraries restored. No world or graphics option was changed.'
