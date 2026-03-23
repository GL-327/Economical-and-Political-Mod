$file = "c:\Users\Gathe\Desktop\Economical-and-Political-Mod\config\political\custom_recipes.json"
$content = [System.IO.File]::ReadAllText($file)

$regex = [System.Text.RegularExpressions.Regex]::new(',"minecraft:max_damage":\d+},count:1,id:"minecraft:', [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
$content = $regex.Replace($content, '}],count:1,id:"minecraft:')

[System.IO.File]::WriteAllText($file, $content)
Write-Host "Done"
