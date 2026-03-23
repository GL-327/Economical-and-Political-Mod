$file = "c:\Users\Gathe\Desktop\Economical-and-Political-Mod\config\political\custom_recipes.json"
$content = [System.IO.File]::ReadAllText($file)

$content = $content -replace ',"minecraft:max_damage":500},count:1,id:"minecraft:', '}],count:1,id:"minecraft:'
$content = $content -replace ',"minecraft:max_damage":\d+},count:1,id:"minecraft:', '}],count:1,id:"minecraft:'

[System.IO.File]::WriteAllText($file, $content)
Write-Host "Done"
