# SPDX-License-Identifier: LGPL-3.0-only
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-PatchJdk {
    param([string]$JdkHome)
    if ([string]::IsNullOrWhiteSpace($JdkHome)) {
        throw 'Set JAVA_HOME to a JDK 8 or pass -JdkHome. A JRE does not include javac.'
    }
    $java = Join-Path $JdkHome 'bin\java.exe'
    $javac = Join-Path $JdkHome 'bin\javac.exe'
    if (!(Test-Path -LiteralPath $java -PathType Leaf) -or !(Test-Path -LiteralPath $javac -PathType Leaf)) {
        throw "JDK tools not found under $JdkHome. Supply the JDK root, not its bin folder."
    }
    # Java 8 prints its version to stderr; avoid PowerShell 5 treating that as a terminating error.
    $previousPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $version = (& $javac -version 2>&1 | Out-String)
        $versionExit = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousPreference
    }
    if ($versionExit -ne 0 -or $version -notmatch 'javac 1\.8\.') {
        throw 'The tested overlay build requires JDK 8. See BUILDING.md for the separate modern Gradle build.'
    }
    return @{ Java = $java; Javac = $javac }
}

function Get-PatchDependencies {
    param([string]$Repository, [string]$Directory, [switch]$Offline)
    New-Item -ItemType Directory -Path $Directory -Force | Out-Null
    $result = @{}
    $manifest = Get-Content -LiteralPath (Join-Path $Repository 'hbm-patch\dependencies.json') -Raw | ConvertFrom-Json
    foreach ($dependency in $manifest) {
        $path = Join-Path $Directory $dependency.file
        if (!(Test-Path -LiteralPath $path -PathType Leaf)) {
            if ($Offline) { throw "Missing dependency: $path. Run without -Offline to download it." }
            Write-Host "Downloading $($dependency.file)"
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
            Invoke-WebRequest -UseBasicParsing -Uri $dependency.url -OutFile $path
        }
        $actual = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash
        if ($actual -ne $dependency.sha256) {
            throw "SHA-256 mismatch for $($dependency.file). Expected $($dependency.sha256), got $actual."
        }
        $result[$dependency.file] = (Resolve-Path -LiteralPath $path).Path
    }
    return $result
}
