$file = "c:\Users\Gathe\Desktop\Economical-and-Political-Mod\config\political\custom_recipes.json"
$content = [System.IO.File]::ReadAllText($file)

$matches = [System.Text.RegularExpressions.Regex]::Matches($content, ',"minecraft:max_damage":\d+\},count:1,id:"minecraft:')
Write-Host "Found $($matches.Count) matches"

foreach ($match in $matches) {
    $content = $content.Replace($match.Value, '}],count:1,id:"minecraft:')
}

[System.IO.File]::WriteAllText($file, $content)
Write-Host "Done"
