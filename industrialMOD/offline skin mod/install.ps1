param([string]$MinecraftDirectory = 'D:\programs\Maincraft\Minecraft\game')
$ErrorActionPreference = 'Stop'
$gamePath = [IO.Path]::GetFullPath($MinecraftDirectory)
$modsPath = Join-Path $gamePath 'mods'
if (!(Test-Path -LiteralPath $modsPath -PathType Container)) { throw 'Minecraft mods directory was not found.' }
$runningGames = @(Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" | Where-Object {
    $_.CommandLine -match 'launchwrapper\.Launch|net\.minecraft\.client\.main|ServerLaunchWrapper|MinecraftServer'
})
if ($runningGames.Count -gt 0) { throw 'Close Minecraft before installing. No processes were stopped and no files were changed.' }
$sourceJar = Join-Path $PSScriptRoot 'build\libs\OfflineSkins-1.7.10-1.2.0.jar'
if (!(Test-Path -LiteralPath $sourceJar)) { throw 'Build the mod first, or install the release JAR manually.' }
$destination = Join-Path $modsPath (Split-Path $sourceJar -Leaf)
$previous = @(Get-ChildItem -LiteralPath $modsPath -File | Where-Object { $_.Name -match '^OfflineSkins.*\.jar$' })
if ($previous.Count -gt 0) {
    $backup = Join-Path $gamePath ('mod-backups\offlineskins-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
    New-Item -ItemType Directory -Path $backup | Out-Null
    foreach ($jar in $previous) { Move-Item -LiteralPath $jar.FullName -Destination (Join-Path $backup $jar.Name) }
}
Copy-Item -LiteralPath $sourceJar -Destination $destination
$skinDirectory = Join-Path $gamePath 'config\offlineskins\skins'
New-Item -ItemType Directory -Path $skinDirectory -Force | Out-Null
$helpPath = Join-Path $skinDirectory 'PUT-SKINS-HERE.txt'
if (!(Test-Path -LiteralPath $helpPath)) {
    [IO.File]::WriteAllText($helpPath, "Put your own skin PNG here and name it skin.png.`r`nStart Minecraft and press F8, then Refresh.`r`n64x32 or 64x64 PNG, maximum 24 KiB. Classic Steve model.`r`nUse the same mod on the server and other clients to share skins.`r`n")
}
if ((Get-FileHash -LiteralPath $sourceJar).Hash -ne (Get-FileHash -LiteralPath $destination).Hash) { throw 'Installed JAR hash does not match the release.' }
[pscustomobject]@{ InstalledJar = $destination; SkinFolder = $skinDirectory; SHA256 = (Get-FileHash -LiteralPath $destination).Hash }
