param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern("^phase-.+$")]
    [string] $Phase,

    [Parameter(Mandatory = $true)]
    [ValidateSet("idle", "running", "planning", "implementing", "verifying", "fixing", "committing", "blocked", "committed")]
    [string] $Status,

    [Parameter(Mandatory = $true)]
    [ValidateSet("run", "plan", "implement", "verify", "fix", "commit", "block", "reset")]
    [string] $Command,

    [string] $Notes = ""
)

$ErrorActionPreference = "Stop"
$StateFile = Join-Path $PSScriptRoot "..\..\_workspace\codex\state.json"
$StateDir = Split-Path -Parent $StateFile

if (-not (Test-Path -LiteralPath $StateDir)) {
    New-Item -ItemType Directory -Force -Path $StateDir | Out-Null
}

if ($Status -eq "idle") {
    $phaseValue = $null
} else {
    $phaseValue = $Phase
}

$state = [ordered] @{
    phase = $phaseValue
    status = $Status
    last_command = $Command
    updated_at = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    notes = $Notes
}

$json = $state | ConvertTo-Json -Depth 4
Set-Content -Encoding UTF8 -LiteralPath $StateFile -Value $json

Write-Host "[HARNESS STATE] phase=$phaseValue status=$Status command=$Command"
