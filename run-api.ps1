Write-Host "--- Starting Smart Room AI API ---" -ForegroundColor Cyan

# Check if python is installed
if (!(Get-Command python -ErrorAction SilentlyContinue)) {
    Write-Error "Python is not installed or not in PATH."
    exit
}

# Install dependencies if needed
Write-Host "Checking dependencies..." -ForegroundColor Yellow
pip install -r python_api/requirements.txt --quiet

# Run the API
Write-Host "Starting server on http://localhost:8003..." -ForegroundColor Green
python python_api/main.py
