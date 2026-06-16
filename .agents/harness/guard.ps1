param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("run", "commit")]
    [string] $Command,

    [Parameter(Mandatory = $true)]
    [ValidatePattern("^phase-.+$")]
    [string] $Phase,

    [Parameter(Mandatory = $true)]
    [ValidateSet("start", "finish")]
    [string] $Stage
)

$ErrorActionPreference = "Stop"
$Root = Resolve-Path (Join-Path $PSScriptRoot "..\..")

function Assert-ArtifactExists([string] $RelativePath) {
    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path)) {
        throw "[HARNESS BLOCKED] Missing required artifact: $RelativePath"
    }
}

function Assert-FeatureBranch {
    $branch = git -C $Root branch --show-current
    if ($branch -notmatch "^feature\/.+") {
        throw "[HARNESS BLOCKED] Work must run on a feature branch. Current branch: $branch"
    }
}

function Assert-VerificationPassed {
    $verification = "_workspace\codex\$Phase\verification.md"
    Assert-ArtifactExists $verification
    $content = Get-Content -Encoding UTF8 -Raw -LiteralPath (Join-Path $Root $verification)
    if ($content -notmatch "gradlew(\.bat)? test:\s*PASS") {
        throw "[HARNESS BLOCKED] verification.md does not contain 'gradlew test: PASS'."
    }
}

function Assert-CommitInputsReady {
    Assert-ArtifactExists "_workspace\codex\$Phase\implementation.md"
    Assert-ArtifactExists "_workspace\codex\$Phase\verification.md"
    Assert-ArtifactExists "PLAN.md"
    Assert-ArtifactExists "AGENTS.md"
    Assert-ArtifactExists ".codex\config.toml"
    Assert-ArtifactExists ".agents\skills\codex-orchestrator\SKILL.md"
    Assert-ArtifactExists ".agents\harness\guard.ps1"
    Assert-ArtifactExists ".githooks\pre-commit"
}

if ($Command -eq "run" -and $Stage -eq "start") {
    Assert-FeatureBranch
}

if ($Command -eq "commit") {
    Assert-FeatureBranch
    Assert-CommitInputsReady
    Assert-VerificationPassed
}

if ($Command -eq "run" -and $Stage -eq "finish") {
    Assert-VerificationPassed
}

Write-Host "[HARNESS OK] $Command $Phase $Stage"
