# PowerShell script to update timestamp placeholders with actual file modification times
# Usage: .\update-timestamps.ps1

Write-Host "Updating timestamp placeholders in files..." -ForegroundColor Green

# Find all files containing the unique discriminator pattern
# This pattern works across all programming languages and comment styles
$files = Get-ChildItem -Path "src" -Recurse -File | Where-Object {
    $content = Get-Content $_.FullName -ErrorAction SilentlyContinue | Out-String
    # Look for the unique discriminator that works in any language
    # Using a very specific pattern that won't conflict with any code
    # Supports multiple comment styles: //, /* */, <!-- -->, #, etc.
    $content -match "LLM_EDIT_TIMESTAMP:" -and ($content -match "LLM_EDIT_TIMESTAMP: \[TIMESTAMP_PLACEHOLDER\]" -or $content -match "LLM_EDIT_TIMESTAMP: \d{1,2} [A-Za-z]{3,4}\. \d{1,2}:\d{2}")
}

$updatedCount = 0

foreach ($file in $files) {
    try {
        # Get file modification time
        $lastModified = $file.LastWriteTime.ToString("dd MMM HH:mm")
        
        # Read file content
        $content = Get-Content $file.FullName -ErrorAction SilentlyContinue | Out-String
        
        # Replace either placeholder or existing timestamp with new timestamp
        # Using a very specific pattern that won't conflict with any code
        # This works for any comment style: //, /* */, <!-- -->, #, etc.
        $newContent = $content -replace "LLM_EDIT_TIMESTAMP: \[TIMESTAMP_PLACEHOLDER\]", "LLM_EDIT_TIMESTAMP: $lastModified"
        $newContent = $newContent -replace "LLM_EDIT_TIMESTAMP: \d{1,2} [A-Za-z]{3,4}\. \d{1,2}:\d{2}", "LLM_EDIT_TIMESTAMP: $lastModified"
        
        # Write back to file
        Set-Content -Path $file.FullName -Value $newContent -NoNewline
        
        Write-Host "Updated: $($file.FullName) - $lastModified" -ForegroundColor Yellow
        $updatedCount++
    }
    catch {
        Write-Host "Error updating $($file.FullName): $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`nCompleted! Updated $updatedCount files." -ForegroundColor Green