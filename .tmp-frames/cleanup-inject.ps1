$dir = 'F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-frontend\src\icons\iconfont\modules'
$files = Get-ChildItem "$dir\*.ts" | Where-Object { $_.Name -ne 'index.ts' -and $_.Name -ne 'inject-svg.ts' }
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace "import \{ injectSvg \} from '\./inject-svg'\r?\n", ''
    $content = $content -replace "\r?\n\r?\ninjectSvg\(svg, key\)\r?\n", "`n"
    Set-Content $file.FullName -Value $content -NoNewline
    Write-Host "Cleaned: $($file.Name)"
}
