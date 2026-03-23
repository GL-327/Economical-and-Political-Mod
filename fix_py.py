import re

file_path = r'c:\Users\Gathe\Desktop\Economical-and-Political-Mod\config\political\custom_recipes.json'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r',"minecraft:max_damage":\d+\},count:1,id:"minecraft:'
replacement = '}],count:1,id:"minecraft:'
content = re.sub(pattern, replacement, content)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Done")
