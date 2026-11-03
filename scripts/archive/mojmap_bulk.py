#!/usr/bin/env python3
"""Bulk Yarn-ish -> Mojang 26.1 renames for PoliticalServer mod sources."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "java"

# Order: longer / more specific first
REPLACEMENTS: list[tuple[str, str]] = [
    ("Text.translatable(", "Component.translatable("),
    ("Text.literal(", "Component.literal("),
    ("Text.empty()", "Component.empty()"),
    ("Text.keybind(", "Component.keybind("),
    ("MutableText", "MutableComponent"),
    ("import net.minecraft.text.MutableText;", "import net.minecraft.network.chat.MutableComponent;"),
    ("import net.minecraft.text.Text;", "import net.minecraft.network.chat.Component;"),
    ("net.minecraft.text.Text", "net.minecraft.network.chat.Component"),
    ("import net.minecraft.util.Formatting;", "import net.minecraft.ChatFormatting;"),
    ("net.minecraft.util.Formatting", "net.minecraft.ChatFormatting"),
    ("Formatting.", "ChatFormatting."),
    ("import net.minecraft.component.DataComponentTypes;", "import net.minecraft.core.component.DataComponents;"),
    ("net.minecraft.component.DataComponentTypes", "net.minecraft.core.component.DataComponents"),
    ("import net.minecraft.component.type.NbtComponent;", "import net.minecraft.world.item.component.CustomData;"),
    ("import net.minecraft.component.type.*;", "import net.minecraft.world.item.component.*;"),
    ("net.minecraft.component.type.", "net.minecraft.world.item.component."),
    ("new LoreComponent(", "new ItemLore("),
    ("LoreComponent", "ItemLore"),
    ("NbtComponent", "CustomData"),
    ("DyedColorComponent", "DyedItemColor"),
    ("AttributeModifiersComponent", "ItemAttributeModifiers"),
    ("AttributeModifierSlot", "EquipmentSlotGroup"),
    ("import net.minecraft.entity.attribute.EntityAttributeModifier;", "import net.minecraft.world.entity.ai.attributes.AttributeModifier;"),
    ("import net.minecraft.entity.attribute.EntityAttributes;", "import net.minecraft.world.entity.ai.attributes.Attributes;"),
    ("net.minecraft.entity.attribute.EntityAttributeModifier", "net.minecraft.world.entity.ai.attributes.AttributeModifier"),
    ("net.minecraft.entity.attribute.EntityAttributes", "net.minecraft.world.entity.ai.attributes.Attributes"),
    ("EntityAttributeModifier.Operation", "AttributeModifier.Operation"),
    ("import net.minecraft.entity.EquipmentSlot;", "import net.minecraft.world.entity.EquipmentSlot;"),
    ("import net.minecraft.entity.LivingEntity;", "import net.minecraft.world.entity.LivingEntity;"),
    ("import net.minecraft.entity.Entity;", "import net.minecraft.world.entity.Entity;"),
    ("import net.minecraft.entity.player.PlayerEntity;", "import net.minecraft.world.entity.player.Player;"),
    ("net.minecraft.entity.player.PlayerEntity", "net.minecraft.world.entity.player.Player"),
    ("import net.minecraft.entity.effect.StatusEffectInstance;", "import net.minecraft.world.effect.MobEffectInstance;"),
    ("import net.minecraft.entity.effect.StatusEffects;", "import net.minecraft.world.effect.MobEffects;"),
    ("import net.minecraft.entity.effect.StatusEffect;", "import net.minecraft.world.effect.MobEffect;"),
    ("StatusEffectInstance", "MobEffectInstance"),
    ("StatusEffects.", "MobEffects."),
    ("StatusEffect", "MobEffect"),
    ("import net.minecraft.item.", "import net.minecraft.world.item."),
    ("net.minecraft.item.", "net.minecraft.world.item."),
    ("import net.minecraft.block.", "import net.minecraft.world.level.block."),
    ("net.minecraft.block.", "net.minecraft.world.level.block."),
    ("import net.minecraft.screen.ScreenHandlerType;", "import net.minecraft.world.inventory.MenuType;"),
    ("import net.minecraft.screen.", "import net.minecraft.world.inventory."),
    ("net.minecraft.screen.ScreenHandlerType", "net.minecraft.world.inventory.MenuType"),
    ("net.minecraft.screen.", "net.minecraft.world.inventory."),
    ("import net.minecraft.server.network.ServerPlayerEntity;", "import net.minecraft.server.level.ServerPlayer;"),
    ("net.minecraft.server.network.ServerPlayerEntity", "net.minecraft.server.level.ServerPlayer"),
    ("import net.minecraft.server.world.ServerWorld;", "import net.minecraft.server.level.ServerLevel;"),
    ("net.minecraft.server.world.ServerWorld", "net.minecraft.server.level.ServerLevel"),
    ("import net.minecraft.util.math.BlockPos;", "import net.minecraft.core.BlockPos;"),
    ("net.minecraft.util.math.BlockPos", "net.minecraft.core.BlockPos"),
    ("import net.minecraft.util.math.Vec3d;", "import net.minecraft.world.phys.Vec3;"),
    ("net.minecraft.util.math.Vec3d", "net.minecraft.world.phys.Vec3"),
    ("import net.minecraft.util.math.Direction;", "import net.minecraft.core.Direction;"),
    ("net.minecraft.util.math.Direction", "net.minecraft.core.Direction"),
    ("import net.minecraft.util.Identifier;", "import net.minecraft.resources.ResourceLocation;"),
    ("net.minecraft.util.Identifier", "net.minecraft.resources.ResourceLocation"),
    ("Identifier.of(", "ResourceLocation.fromNamespaceAndPath("),
    ("Identifier.tryParse(", "ResourceLocation.tryParse("),
    ("import net.minecraft.registry.Registries;", "import net.minecraft.core.registries.BuiltInRegistries;"),
    ("net.minecraft.registry.Registries", "net.minecraft.core.registries.BuiltInRegistries"),
    ("import net.minecraft.registry.RegistryKey;", "import net.minecraft.resources.ResourceKey;"),
    ("import net.minecraft.registry.RegistryKeys;", "import net.minecraft.core.registries.Registries;"),
    ("RegistryKey<", "ResourceKey<"),
    ("RegistryKey.", "ResourceKey."),
    ("RegistryKeys.", "Registries."),
    ("RegistryKey.of(", "ResourceKey.create("),
    # Fix double Registries for ENCHANTMENT etc. — RegistryKeys.ENCHANTMENT -> Registries.ENCHANTMENT
    ("import net.minecraft.registry.Registry;", "import net.minecraft.core.Registry;"),
    ("import net.minecraft.registry.entry.RegistryEntry;", "import net.minecraft.core.Holder;"),
    ("RegistryEntry", "Holder"),
    ("import net.minecraft.util.Hand;", "import net.minecraft.world.InteractionHand;"),
    ("net.minecraft.util.Hand", "net.minecraft.world.InteractionHand"),
    ("Hand.", "InteractionHand."),
    ("import net.minecraft.nbt.", "import net.minecraft.nbt."),
    ("import net.minecraft.sound.SoundEvents;", "import net.minecraft.sounds.SoundEvents;"),
    ("net.minecraft.sound.SoundEvents", "net.minecraft.sounds.SoundEvents"),
    ("import net.minecraft.sound.SoundCategory;", "import net.minecraft.sounds.SoundSource;"),
    ("net.minecraft.sound.SoundCategory", "net.minecraft.sounds.SoundSource"),
    ("SoundCategory.", "SoundSource."),
    ("import net.minecraft.particle.ParticleTypes;", "import net.minecraft.core.particles.ParticleTypes;"),
    ("net.minecraft.particle.ParticleTypes", "net.minecraft.core.particles.ParticleTypes"),
    ("import net.minecraft.particle.", "import net.minecraft.core.particles."),
    ("net.minecraft.particle.", "net.minecraft.core.particles."),
    ("import net.minecraft.world.World;", "import net.minecraft.world.level.Level;"),
    ("net.minecraft.world.World", "net.minecraft.world.level.Level"),
    ("World.OVERWORLD", "Level.OVERWORLD"),
    ("World.NETHER", "Level.NETHER"),
    ("World.END", "Level.END"),
    ("import net.minecraft.util.math.GlobalPos;", "import net.minecraft.core.GlobalPos;"),
    ("net.minecraft.util.math.GlobalPos", "net.minecraft.core.GlobalPos"),
    ("import net.minecraft.command.argument.", "import net.minecraft.commands.arguments."),
    ("net.minecraft.command.argument.", "net.minecraft.commands.arguments."),
    ("import net.minecraft.server.command.", "import net.minecraft.commands."),
    ("net.minecraft.server.command.", "net.minecraft.commands."),
    ("ServerCommandSource", "CommandSourceStack"),
    ("import net.minecraft.server.MinecraftServer;", "import net.minecraft.server.MinecraftServer;"),
    ("import net.minecraft.world.Difficulty;", "import net.minecraft.world.Difficulty;"),
    ("import net.minecraft.entity.damage.", "import net.minecraft.world.damagesource."),
    ("net.minecraft.entity.damage.", "net.minecraft.world.damagesource."),
    ("import net.minecraft.entity.projectile.", "import net.minecraft.world.entity.projectile."),
    ("net.minecraft.entity.projectile.", "net.minecraft.world.entity.projectile."),
    ("import net.minecraft.entity.mob.", "import net.minecraft.world.entity.monster."),
    ("net.minecraft.entity.mob.", "net.minecraft.world.entity.monster."),
    ("import net.minecraft.entity.passive.", "import net.minecraft.world.entity.animal."),
    ("net.minecraft.entity.passive.", "net.minecraft.world.entity.animal."),
    ("import net.minecraft.entity.boss.", "import net.minecraft.world.entity.boss."),
    ("net.minecraft.entity.boss.", "net.minecraft.world.entity.boss."),
    ("import net.minecraft.entity.decoration.", "import net.minecraft.world.entity.decoration."),
    ("net.minecraft.entity.decoration.", "import net.minecraft.world.entity.decoration."),
    ("import net.minecraft.entity.vehicle.", "import net.minecraft.world.entity.vehicle."),
    ("net.minecraft.entity.vehicle.", "net.minecraft.world.entity.vehicle."),
    ("import net.minecraft.entity.ai.goal.", "import net.minecraft.world.entity.ai.goal."),
    ("net.minecraft.entity.ai.goal.", "net.minecraft.world.entity.ai.goal."),
    ("import net.minecraft.village.", "import net.minecraft.world.entity.npc."),
    ("net.minecraft.village.", "net.minecraft.world.entity.npc."),
    ("import net.minecraft.entity.ai.pathing.", "import net.minecraft.world.entity.ai.navigation."),
    ("net.minecraft.entity.ai.pathing.", "import net.minecraft.world.entity.ai.navigation."),
    ("import net.minecraft.entity.ai.control.", "import net.minecraft.world.entity.ai.control."),
    ("net.minecraft.entity.ai.control.", "net.minecraft.world.entity.ai.control."),
    ("import net.minecraft.entity.ai.brain.", "import net.minecraft.world.entity.ai.brain."),
    ("net.minecraft.entity.ai.brain.", "import net.minecraft.world.entity.ai.brain."),
    ("import net.minecraft.entity.ai.", "import net.minecraft.world.entity.ai."),
    ("net.minecraft.entity.ai.", "net.minecraft.world.entity.ai."),
    ("import net.minecraft.entity.attribute.", "import net.minecraft.world.entity.ai.attributes."),
    ("net.minecraft.entity.attribute.", "net.minecraft.world.entity.ai.attributes."),
    ("import net.minecraft.entity.data.", "import net.minecraft.network.syncher."),
    ("net.minecraft.entity.data.", "net.minecraft.network.syncher."),
    ("import net.minecraft.entity.spawn.", "import net.minecraft.world.entity."),
    ("net.minecraft.entity.spawn.", "net.minecraft.world.entity."),
    ("import net.minecraft.entity.", "import net.minecraft.world.entity."),
    # Avoid double-replacing world.entity paths already set
]

EXTRA_AFTER = [
    (".getEntityWorld()", ".level()"),
    ("getRegistryKey()", "dimension()"),
]


def process_file(path: Path) -> bool:
    s = path.read_text(encoding="utf-8")
    orig = s
    for a, b in REPLACEMENTS:
        s = s.replace(a, b)
    for a, b in EXTRA_AFTER:
        s = s.replace(a, b)
    # ResourceKey.create takes (ResourceKey<Registry<T>>, ResourceLocation) in Mojang
    s = s.replace("ResourceKey.create(Registries.WORLD, ResourceLocation.fromNamespaceAndPath(\"minecraft:the_nether\"))",
                  "ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace(\"the_nether\"))")
    s = s.replace("ResourceKey.create(Registries.WORLD, ResourceLocation.fromNamespaceAndPath(\"minecraft:the_end\"))",
                  "ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace(\"the_end\"))")
    s = s.replace("ResourceKey.create(Registries.WORLD, ResourceLocation.fromNamespaceAndPath(\"minecraft:overworld\"))",
                  "ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace(\"overworld\"))")
    if s != orig:
        path.write_text(s, encoding="utf-8")
        return True
    return False


def main() -> None:
    n = 0
    for path in ROOT.rglob("*.java"):
        if ".backup" in path.name:
            continue
        if process_file(path):
            n += 1
    print(f"Updated {n} files", file=sys.stderr)


if __name__ == "__main__":
    main()
