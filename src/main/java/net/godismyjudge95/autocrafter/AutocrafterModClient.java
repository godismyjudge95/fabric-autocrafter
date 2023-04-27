package net.godismyjudge95.autocrafter;

import net.fabricmc.api.ClientModInitializer;
import net.godismyjudge95.autocrafter.screen.AutocrafterScreen;
import net.godismyjudge95.autocrafter.screen.ModScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class AutocrafterModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.AUTOCRAFTER_SCREEN_HANDLER, AutocrafterScreen::new);
    }
}
