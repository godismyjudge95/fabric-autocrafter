package net.godismyjudge95.autocrafter.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.godismyjudge95.autocrafter.AutocrafterMod;
import net.godismyjudge95.autocrafter.block.custom.AutocrafterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block AUTOCRAFTER_BLOCK = registerBlock(
            "autocrafter_block",
            new AutocrafterBlock(
                    FabricBlockSettings.of(Material.STONE).sounds(BlockSoundGroup.STONE).strength(3.5F)),
            ItemGroups.REDSTONE,
            Items.REDSTONE);

    @SuppressWarnings("SameParameterValue")
    private static Block registerBlock(String name, Block block, ItemGroup tab, Item after) {
        registerBlockItem(name, block, tab, after);

        return Registry.register(
                Registries.BLOCK,
                new Identifier(AutocrafterMod.MOD_ID, name),
                block);
    }

    private static void registerBlockItem(String name, Block block, ItemGroup tab, Item after) {
        Item item = Registry.register(
                Registries.ITEM,
                new Identifier(AutocrafterMod.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));

        //noinspection UnstableApiUsage
        ItemGroupEvents.modifyEntriesEvent(tab).register(content -> content.addAfter(after, item));
    }

    public static void registerModBlocks() {
        AutocrafterMod.LOGGER.info("Registering ModBlocks for " + AutocrafterMod.MOD_ID);
    }
}
