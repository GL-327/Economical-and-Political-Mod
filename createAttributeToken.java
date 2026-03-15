public static ItemStack createAttributeToken(ArmourAttribute attr) {
        ItemStack token = new ItemStack(Items.PAPER);
        token.set(DataComponentTypes.CUSTOM_NAME,
                Text.literal("✧ " + attr.displayName + " Attribute Token ✧")
                        .formatted(attr.color, Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(""));
        lore.add(Text.literal("◆ LEGENDARY ATTRIBUTE").formatted(attr.color, Formatting.BOLD));
        lore.add(Text.literal(""));
        
        // Add thematic lore based on attribute type
        switch (attr) {
            case BURNING -> {
                lore.add(Text.literal("A crystallized fragment of eternal flame").formatted(Formatting.GRAY));
                lore.add(Text.literal("forged in deepest Nether fires.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Harness power of perpetual combustion").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and become immune to extinguishment.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"If you want to shine like the sun,").formatted(Formatting.GOLD, Formatting.ITALIC));
                lore.add(Text.literal("first you have to burn like it.\"").formatted(Formatting.GOLD, Formatting.ITALIC));
                break;
            }
            case SIGHTLESS -> {
                lore.add(Text.literal("An essence of pure darkness").formatted(Formatting.GRAY));
                lore.add(Text.literal("whispered by blind cave dwellers.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("See beyond mortal sight limitations").formatted(Formatting.YELLOW));
                lore.add(Text.literal("while immune to visual impairments.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"In darkness, true vision begins.\"").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
                break;
            }
            case FRENZIED -> {
                lore.add(Text.literal("The raw rage of ancient warriors").formatted(Formatting.GRAY));
                lore.add(Text.literal("distilled into crystalline form.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Unleash berserk fury in combat").formatted(Formatting.YELLOW));
                lore.add(Text.literal("with enhanced attack speed.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Rage is the weapon of the fierce.\"").formatted(Formatting.RED, Formatting.ITALIC));
                break;
            }
            case GROUNDED -> {
                lore.add(Text.literal("Earth's steadfast protection").formatted(Formatting.GRAY));
                lore.add(Text.literal("solidified into defensive crystal.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Stand firm against lightning strikes").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and deflect incoming projectiles.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"As steady as the mountain itself.\"").formatted(Formatting.GREEN, Formatting.ITALIC));
                break;
            }
            case WEBBED -> {
                lore.add(Text.literal("Spider silk woven with magic").formatted(Formatting.GRAY));
                lore.add(Text.literal("enchanted by ancient arachnids.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Move freely through cobweb barriers").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and gain spider neutrality.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"The web catches all but the spider.\"").formatted(Formatting.DARK_GREEN, Formatting.ITALIC));
                break;
            }
            case FROST -> {
                lore.add(Text.literal("Frozen essence of ice giants").formatted(Formatting.GRAY));
                lore.add(Text.literal("preserved in eternal winter.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Walk on water as frozen ground").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and resist the cold embrace.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Winter's fury, summer's bane.\"").formatted(Formatting.AQUA, Formatting.ITALIC));
                break;
            }
            case PHANTOMSTEP -> {
                lore.add(Text.literal("Ethereal essence of phantoms").formatted(Formatting.GRAY));
                lore.add(Text.literal("captured from silent hunters.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Walk with spectral silence").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and laugh at gravity's pull.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Between steps, there is only silence.\"").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
                break;
            }
            case CURSED -> {
                lore.add(Text.literal("Dark power of forbidden knowledge").formatted(Formatting.GRAY));
                lore.add(Text.literal("bound in blood magic parchment.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Steal life force with each kill").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and embrace vampiric power.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"Life given, life taken away.\"").formatted(Formatting.DARK_RED, Formatting.ITALIC));
                break;
            }
            case OVERGROWN -> {
                lore.add(Text.literal("Nature's healing crystallized").formatted(Formatting.GRAY));
                lore.add(Text.literal("blessed by ancient forest spirits.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Regenerate on natural ground").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and channel earth's vitality.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"From earth we come, to earth we return.\"").formatted(Formatting.GREEN, Formatting.ITALIC));
                break;
            }
            case VOLATILE -> {
                lore.add(Text.literal("Explosive power of chaos").formatted(Formatting.GRAY));
                lore.add(Text.literal("contained in unstable crystal.").formatted(Formatting.DARK_GRAY));
                lore.add(Text.literal(""));
                lore.add(Text.literal("Detonate when near death").formatted(Formatting.YELLOW));
                lore.add(Text.literal("and become blast immune.").formatted(Formatting.YELLOW));
                lore.add(Text.literal(""));
                lore.add(Text.literal("\"In destruction, there is creation.\"").formatted(Formatting.RED, Formatting.ITALIC));
                break;
            }
        }
        
        lore.add(Text.literal(""));
        lore.add(Text.literal("━━━ USAGE ━━━").formatted(Formatting.GOLD));
        lore.add(Text.literal("Apply to: ").formatted(Formatting.GRAY)
                .append(Text.literal(attr.slot == null ? "Any Armor Piece" : attr.slot.getName().toUpperCase())
                        .formatted(Formatting.AQUA)));
        lore.add(Text.literal("XP Cost: ").formatted(Formatting.RED)
                .append(Text.literal(attr.xpCost + " levels").formatted(Formatting.BOLD, Formatting.RED)));
        lore.add(Text.literal(""));
        lore.add(Text.literal("⚔ Combine with armor in anvil").formatted(Formatting.YELLOW));
        
        token.set(DataComponentTypes.LORE, new LoreComponent(lore));
        token.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        NbtCompound nbt = new NbtCompound();
        nbt.putString(ARMOR_ATTRIBUTE_TOKEN_KEY, attr.id);
        token.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        
        setCustomItemId(token, "attribute_token_" + attr.id);
        token.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return token;
    }
