param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("run", "plan", "implement", "verify", "fix", "commit", "status")]
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
$StateFile = Join-Path $Root "_workspace\codex\state.json"

function Read-HarnessState {
    if (-not (Test-Path -LiteralPath $StateFile)) {
        throw "[HARNESS BLOCKED] Missing _workspace/codex/state.json."
    }
    return Get-Content -Encoding UTF8 -Raw -LiteralPath $StateFile | ConvertFrom-Json
}

function Assert-ArtifactExists([string] $RelativePath) {
    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path)) {
        throw "[HARNESS BLOCKED] Missing required artifact: $RelativePath"
    }
}

function Assert-PhaseMatches($State) {
    if ($State.phase -and $State.phase -ne $Phase) {
        throw "[HARNESS BLOCKED] Current phase is '$($State.phase)', not '$Phase'."
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
    Assert-ArtifactExists "_workspace\codex\state.json"
    Assert-ArtifactExists "PLAN.md"
    Assert-ArtifactExists "AGENTS.md"
    Assert-ArtifactExists ".codex\config.toml"
    Assert-ArtifactExists ".agents\skills\codex-orchestrator\SKILL.md"
    Assert-ArtifactExists ".agents\harness\guard.ps1"
    Assert-ArtifactExists ".agents\harness\set-state.ps1"
}

$state = Read-HarnessState
$status = [string] $state.status

if ($Command -eq "status") {
    Write-Host "[HARNESS STATUS] phase=$($state.phase) status=$($state.status) last_command=$($state.last_command)"
    exit 0
}

if ($Stage -eq "start") {
    switch ($Command) {
        "run" {
            if ($status -notin @("idle", "committed", "blocked")) {
                throw "[HARNESS BLOCKED] /run requires idle, committed, or blocked status. Current: $status"
            }
        }
        "plan" {
            Assert-PhaseMatches $state
            if ($status -notin @("running", "blocked")) {
                throw "[HARNESS BLOCKED] plan requires running or blocked status. Current: $status"
            }
        }
        "implement" {
            Assert-PhaseMatches $state
            Assert-FeatureBranch
            if ($status -ne "planning") {
                throw "[HARNESS BLOCKED] implement requires planning status. Current: $status"
            }
            Assert-ArtifactExists "PLAN.md"
        }
        "verify" {
            Assert-PhaseMatches $state
            if ($status -notin @("implementing", "fixing")) {
                throw "[HARNESS BLOCKED] verify requires implementing or fixing status. Current: $status"
            }
            Assert-ArtifactExists "_workspace\codex\$Phase\implementation.md"
        }
        "fix" {
            Assert-PhaseMatches $state
            Assert-FeatureBranch
            if ($status -ne "verifying") {
                throw "[HARNESS BLOCKED] fix requires verifying status. Current: $status"
            }
        }
        "commit" {
            Assert-PhaseMatches $state
            Assert-FeatureBranch
            if ($status -notin @("verifying", "committing", "committed")) {
                throw "[HARNESS BLOCKED] commit requires verifying, committing, or committed status. Current: $status"
            }
            Assert-CommitInputsReady
            Assert-VerificationPassed
        }
    }
    Write-Host "[HARNESS OK] $Command $Phase start"
    exit 0
}

switch ($Command) {
    "run" {
        Assert-PhaseMatches $state
        if ($status -notin @("committed", "blocked")) {
            throw "[HARNESS BLOCKED] /run finish requires committed or blocked status. Current: $status"
        }
    }
    "plan" {
        Assert-ArtifactExists "PLAN.md"
        if ($status -ne "planning") {
            throw "[HARNESS BLOCKED] plan finish requires planning status. Current: $status"
        }
    }
    "implement" {
        Assert-ArtifactExists "_workspace\codex\$Phase\implementation.md"
        if ($status -ne "implementing") {
            throw "[HARNESS BLOCKED] implement finish requires implementing status. Current: $status"
        }
    }
    "verify" {
        Assert-ArtifactExists "_workspace\codex\$Phase\verification.md"
        if ($status -notin @("verifying", "blocked")) {
            throw "[HARNESS BLOCKED] verify finish requires verifying or blocked status. Current: $status"
        }
        if ($status -eq "verifying") {
            Assert-VerificationPassed
        }
    }
    "fix" {
        if ($status -ne "fixing") {
            throw "[HARNESS BLOCKED] fix finish requires fixing status. Current: $status"
        }
    }
    "commit" {
        if ($status -ne "committed") {
            throw "[HARNESS BLOCKED] commit finish requires committed status. Current: $status"
        }
    }
}

Write-Host "[HARNESS OK] $Command $Phase finish"
