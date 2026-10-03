# ============================================================
#  SBOM Public Service - Command-Line Tools
# ============================================================
#
#  SETUP (one-time):
#    1. Open PowerShell
#    2. Change the BASE_URL below if needed
#    3. Run:  . .\sbom-tools.ps1
#       (note the dot and space before the path - this loads the commands)
#
#  AVAILABLE COMMANDS:
#    uploadAndValidate <filePath> <schemaType> <schemaVersion>
#    resetSession
#    showSession
#
#  EXAMPLES:
#    uploadAndValidate C:\test-data\A1bom.json cdqcydx1.6 1.6
#    uploadAndValidate C:\test-data\b2bom.json cdqspdx2.3 2.3
#    uploadAndValidate C:\test-data\spdxbom.json spdx 2.3
#
#    showSession       # shows current timestamp and index
#    resetSession      # clears session to start fresh
#
# ============================================================

# ===================== CHANGE THIS =========================
# $global:SBOM_BASE_URL = "http://localhost:9053"
$global:SBOM_BASE_URL = "API_PATH"
# ============================================================

# Session state (auto-managed - do not edit)
$global:SBOM_TIMESTAMP = $null
$global:SBOM_INDEX = 0

function uploadAndValidate {
    param(
        [Parameter(Mandatory = $true, Position = 0)]
        [string]$FilePath,

        [Parameter(Mandatory = $true, Position = 1)]
        [string]$SchemaType,

        [Parameter(Mandatory = $true, Position = 2)]
        [string]$SchemaVersion
    )

    # Validate file exists
    if (-not (Test-Path $FilePath)) {
        Write-Host "ERROR: File not found: $FilePath" -ForegroundColor Red
        return
    }

    $fileName = [System.IO.Path]::GetFileName($FilePath)
    $fullPath = (Resolve-Path $FilePath).Path

    # Build postData JSON
    if ($global:SBOM_TIMESTAMP) {
        $postDataJson = '{"schemaType":"' + $SchemaType + '","schemaVersion":"' + $SchemaVersion + '","schema":false,"index":' + $global:SBOM_INDEX + ',"timestamp":"' + $global:SBOM_TIMESTAMP + '","sbomFileName":"' + $fileName + '"}'
    } else {
        $postDataJson = '{"schemaType":"' + $SchemaType + '","schemaVersion":"' + $SchemaVersion + '","schema":false,"index":' + $global:SBOM_INDEX + ',"timestamp":null,"sbomFileName":"' + $fileName + '"}'
    }

    Write-Host ""
    Write-Host "-------------------------------------------" -ForegroundColor Cyan
    Write-Host "  File:        $fileName" -ForegroundColor Cyan
    Write-Host "  Index:       $($global:SBOM_INDEX)" -ForegroundColor Cyan
    Write-Host "  SchemaType:  $SchemaType" -ForegroundColor Cyan
    Write-Host "  Version:     $SchemaVersion" -ForegroundColor Cyan
    if ($global:SBOM_TIMESTAMP) {
        Write-Host "  Timestamp:   $($global:SBOM_TIMESTAMP)" -ForegroundColor Cyan
    } else {
        Write-Host "  Timestamp:   (first call - will be auto-generated)" -ForegroundColor Yellow
    }
    Write-Host "-------------------------------------------" -ForegroundColor Cyan

    try {
        # Build multipart form data using .NET classes (avoids all shell escaping issues)
        $boundary = [System.Guid]::NewGuid().ToString()
        $LF = "`r`n"

        $fileBytes = [System.IO.File]::ReadAllBytes($fullPath)

        $bodyLines = @(
            "--$boundary",
            "Content-Disposition: form-data; name=`"postData`"$LF",
            $postDataJson,
            "--$boundary",
            "Content-Disposition: form-data; name=`"file`"; filename=`"$fileName`"",
            "Content-Type: application/octet-stream$LF"
        ) -join $LF

        $bodyStart = [System.Text.Encoding]::UTF8.GetBytes($bodyLines + $LF)
        $bodyEnd   = [System.Text.Encoding]::UTF8.GetBytes("$LF--$boundary--$LF")

        $bodyStream = New-Object System.IO.MemoryStream
        $bodyStream.Write($bodyStart, 0, $bodyStart.Length)
        $bodyStream.Write($fileBytes, 0, $fileBytes.Length)
        $bodyStream.Write($bodyEnd, 0, $bodyEnd.Length)
        $bodyArray = $bodyStream.ToArray()
        $bodyStream.Close()

        $contentType = "multipart/form-data; boundary=$boundary"

        $webResponse = Invoke-WebRequest -Uri "$($global:SBOM_BASE_URL)/uploadAndValidate" `
            -Method POST `
            -ContentType $contentType `
            -Body $bodyArray `
            -UseBasicParsing

        $response = $webResponse.Content

        if (-not $response) {
            Write-Host "ERROR: No response from server. Is it running at $($global:SBOM_BASE_URL)?" -ForegroundColor Red
            return
        }

        $responseObj = $response | ConvertFrom-Json

        # Capture timestamp from first response
        if ($null -eq $global:SBOM_TIMESTAMP -and $responseObj.timestamp) {
            $global:SBOM_TIMESTAMP = $responseObj.timestamp
            Write-Host "  Session started: $($global:SBOM_TIMESTAMP)" -ForegroundColor Green
        }

        # Display results
        Write-Host ""
        Write-Host "  RESULT:" -ForegroundColor White
        Write-Host "  File:      $($responseObj.sbomFileName)"
        if ($responseObj.valid -eq $true) {
            Write-Host "  Valid:     True" -ForegroundColor Green
        } else {
            Write-Host "  Valid:     False" -ForegroundColor Red
        }
        Write-Host "  FileHash:  $($responseObj.fileHash)"

        if ($responseObj.message) {
            Write-Host "  Message:   $($responseObj.message)" -ForegroundColor Yellow
        }
        if ($responseObj.errorDetails -and $responseObj.errorDetails.Count -gt 0) {
            Write-Host "  Errors:    $($responseObj.errorDetails.Count) issue(s) found" -ForegroundColor Red
        }
        Write-Host "-------------------------------------------" -ForegroundColor Cyan

        # Full JSON response
        Write-Host "  Full Response:" -ForegroundColor DarkGray
        Write-Host ($responseObj | ConvertTo-Json -Depth 10)
        Write-Host ""

        # Auto-increment index for next call
        $global:SBOM_INDEX++

    }
    catch {
        Write-Host "ERROR: Request failed - $_" -ForegroundColor Red
    }
}

function resetSession {
    $global:SBOM_TIMESTAMP = $null
    $global:SBOM_INDEX = 0
    Write-Host "Session cleared. Timestamp and index reset." -ForegroundColor Green
}

function showSession {
    Write-Host ""
    Write-Host "  BASE_URL:   $($global:SBOM_BASE_URL)"
    if ($global:SBOM_TIMESTAMP) {
        Write-Host "  Timestamp:  $($global:SBOM_TIMESTAMP)"
    } else {
        Write-Host "  Timestamp:  (not started)"
    }
    Write-Host "  Next Index: $($global:SBOM_INDEX)"
    Write-Host ""
}

# Startup message
Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "  SBOM Tools loaded successfully!" -ForegroundColor Green
Write-Host "  Server: $($global:SBOM_BASE_URL)" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Commands:" -ForegroundColor White
Write-Host "    uploadAndValidate <filePath> <schemaType> <version>"
Write-Host "    showSession"
Write-Host "    resetSession"
Write-Host ""
Write-Host "  Example:" -ForegroundColor White
Write-Host "    uploadAndValidate C:\test-data\A1bom.json cdqcydx1.6 1.6"
Write-Host ""
