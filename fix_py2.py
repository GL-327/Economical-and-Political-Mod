# -*- coding: utf-8 -*-
import sys
file_path = r'c:\Users\Gathe\Desktop\Economical-and-Political-Mod\config\political\custom_recipes.json'

with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

import re
pattern = r',"minecraft:max_damage":\d+\},count:1,id:"minecraft:'
replacement = '}],count:1,id:"minecraft:'
new_content = re.sub(pattern, replacement, content)

with open(file_path, 'w', encoding='utf-8', errors='ignore') as f:
    f.write(new_content)

print("Done")
