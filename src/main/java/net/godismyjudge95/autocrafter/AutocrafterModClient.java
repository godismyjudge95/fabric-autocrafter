package net.godismyjudge95.autocrafter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.godismyjudge95.autocrafter.screen.AutocrafterScreen;
import net.godismyjudge95.autocrafter.screen.ModScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

@Environment(EnvType.CLIENT)
public class AutocrafterModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.AUTOCRAFTER_SCREEN_HANDLER, AutocrafterScreen::new);
    }
}
