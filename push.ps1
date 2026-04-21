param([string]$tag)

if (-not $tag) {
    Write-Host "Nhập tag"
    exit
}

docker build -t truongikpk/bookstore-book-service:$tag .
docker push truongikpk/bookstore-book-service:$tag

# .\push.ps1 v1