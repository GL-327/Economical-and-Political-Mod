$file = "c:\Users\Gathe\Desktop\Economical-and-Political-Mod\config\political\custom_recipes.json"
$content = [System.IO.File]::ReadAllText($file)

$pattern = '],"minecraft:max_damage":\d+},count:1,id:"minecraft:'
$content = [System.Text.RegularExpressions.Regex]::Replace($content, $pattern, '}],count:1,id:"minecraft:')

[System.IO.File]::WriteAllText($file, $content)
Write-Host "Done"
