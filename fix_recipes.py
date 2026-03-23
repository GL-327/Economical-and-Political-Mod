import re

with open('config/political/custom_recipes.json', 'r', encoding='utf-8') as f:
    content = f.read()

# Remove "minecraft:max_damage":\d+}, pattern from result nbt
# Pattern: ,"minecraft:max_damage":50},count: -> },count:
content = re.sub(r',"minecraft:max_damage":\d+},count:', r'},count:', content)

with open('config/political/custom_recipes.json', 'w', encoding='utf-8') as f:
    f.write(content)

print("Done - fixed max_damage issues")
