package net.godismyjudge95.autocrafter.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.godismyjudge95.autocrafter.AutocrafterMod;
import net.godismyjudge95.autocrafter.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static BlockEntityType<AutocrafterBlockEntity> AUTOCRAFTER;

    public static void registerBlockEntities() {
        AUTOCRAFTER = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(AutocrafterMod.MOD_ID, "autocrafter"),
                FabricBlockEntityTypeBuilder.create(
                        AutocrafterBlockEntity::new,
                        ModBlocks.AUTOCRAFTER_BLOCK).build(null));
    }
}
