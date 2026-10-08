$root = 'F:\project\idea\self\yanggu-code-generator\yanggu-code-generator-frontend'
$files = Get-ChildItem -Recurse -Include *.ts,*.vue (Join-Path $root 'src')
$anyMatches = $files | Select-String -Pattern ': any|as any'
Write-Output ("any_usages=" + $anyMatches.Count)
$tests = Get-ChildItem -Recurse -Include *.spec.ts,*.test.ts (Join-Path $root 'src')
Write-Output ("test_files=" + $tests.Count)
$vueFiles = Get-ChildItem -Recurse -Filter *.vue (Join-Path $root 'src')
Write-Output ("vue_files=" + $vueFiles.Count)
$tsFiles = Get-ChildItem -Recurse -Filter *.ts (Join-Path $root 'src')
Write-Output ("ts_files=" + $tsFiles.Count)
Write-Output "=== any usage detail (first 10) ==="
$anyMatches | Select-Object -First 10 | ForEach-Object { Write-Output ($_.Path.Replace($root + '\', '') + ':' + $_.LineNumber + ' ' + $_.Line.Trim()) }
Write-Output "=== top 5 largest vue files ==="
$vueFiles | ForEach-Object { $l = (Get-Content $_.FullName | Measure-Object -Line).Lines; [PSCustomObject]@{ Lines = $l; Path = $_.FullName.Replace($root + '\', '') } } | Sort-Object Lines -Descending | Select-Object -First 5 | ForEach-Object { Write-Output ($_.Lines.ToString() + '  ' + $_.Path) }
