$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$jdkRoot = (Get-ChildItem (Join-Path $repoRoot '.toolchain\jdk8') -Directory | Select-Object -First 1).FullName
$forgeJar = Join-Path $repoRoot '.gradle-user\caches\minecraft\net\minecraftforge\forge\1.7.10-10.13.4.1614-1.7.10\forgeSrc-1.7.10-10.13.4.1614-1.7.10.jar'
$classpath = $forgeJar + ';' + [IO.File]::ReadAllText((Join-Path $repoRoot '.toolchain\runtime-classpath.txt')).Trim()
$classesDir = Join-Path $PSScriptRoot 'build\classes'
$jarDir = Join-Path $PSScriptRoot 'build'
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
$sources = Get-ChildItem (Join-Path $PSScriptRoot 'src') -Recurse -Filter '*.java' | ForEach-Object FullName
& (Join-Path $jdkRoot 'bin\javac.exe') -source 8 -target 8 -encoding UTF-8 -cp $classpath -d $classesDir @sources
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
$manifestPath = Join-Path $jarDir 'MANIFEST.MF'
[IO.File]::WriteAllText($manifestPath,"Manifest-Version: 1.0`r`nFMLCorePlugin: personal.factory.LoadingPlugin`r`nFMLCorePluginContainsFMLMod: true`r`n`r`n",[Text.UTF8Encoding]::new($false))
$jarPath = Join-Path $jarDir 'PersonalFactoryFix-0.2.4.jar'
& (Join-Path $jdkRoot 'bin\jar.exe') cfm $jarPath $manifestPath -C $classesDir .
if ($LASTEXITCODE -ne 0) { throw 'Packaging failed' }
Get-FileHash -LiteralPath $jarPath
