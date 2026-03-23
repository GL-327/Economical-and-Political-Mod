import re

file_path = r'c:\Users\Gathe\Desktop\Economical-and-Political-Mod\config\political\custom_recipes.json'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Remove all "minecraft:max_damage":X} patterns outside components
content = re.sub(r',"minecraft:max_damage":\d+\}', '}', content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Done")
