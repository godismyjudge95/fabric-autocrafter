package net.godismyjudge95.autocrafter.screen;

import net.minecraft.screen.ScreenHandlerType;

public class ModScreenHandlers {
    public static ScreenHandlerType<AutocrafterScreenHandler> AUTOCRAFTER_SCREEN_HANDLER;

    public static void registerScreenHandlers() {
        AUTOCRAFTER_SCREEN_HANDLER = new ScreenHandlerType<>(AutocrafterScreenHandler::new);
    }
}
