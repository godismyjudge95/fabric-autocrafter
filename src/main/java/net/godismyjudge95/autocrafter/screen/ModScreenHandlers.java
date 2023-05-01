package net.godismyjudge95.autocrafter.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.godismyjudge95.autocrafter.AutocrafterMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public class ModScreenHandlers {
    @SuppressWarnings("deprecation")
    public static ScreenHandlerType<AutocrafterScreenHandler> AUTOCRAFTER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(AutocrafterMod.BLOCK_ID, AutocrafterScreenHandler::new);

    public static void registerScreenHandlers() {

        AutocrafterMod.LOGGER.info("Registering ModScreenHandlers for " + AutocrafterMod.MOD_ID);
    }
}
