package net.godismyjudge95.autocrafter;

import net.fabricmc.api.ModInitializer;
import net.godismyjudge95.autocrafter.block.ModBlocks;
import net.godismyjudge95.autocrafter.block.entity.ModBlockEntities;
import net.godismyjudge95.autocrafter.screen.ModScreenHandlers;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutocrafterMod implements ModInitializer {
    public static final String MOD_ID = "autocrafter";
    public  static  final Identifier BLOCK_ID = new Identifier(MOD_ID, "autocrafter_block");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
//        TODO: Fix this
//        AutocrafterConfig.registerConfigs();

        ModBlocks.registerModBlocks();
        ModBlockEntities.registerBlockEntities();
        ModScreenHandlers.registerScreenHandlers();
    }
}
