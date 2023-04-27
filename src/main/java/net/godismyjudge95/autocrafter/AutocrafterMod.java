package net.godismyjudge95.autocrafter;

import net.fabricmc.api.ModInitializer;
import net.godismyjudge95.autocrafter.block.ModBlocks;
import net.godismyjudge95.autocrafter.block.entity.ModBlockEntities;
import net.godismyjudge95.autocrafter.screen.ModScreenHandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutocrafterMod implements ModInitializer {
    public static final String MOD_ID = "autocrafter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AutocrafterConfig.registerConfigs();

        ModBlocks.registerModBlocks();
        ModBlockEntities.registerBlockEntities();
        ModScreenHandlers.registerScreenHandlers();
    }
}
