package net.godismyjudge95.autocrafter;

import com.mojang.datafixers.util.Pair;
import net.godismyjudge95.autocrafter.helpers.SimpleConfig;

public class AutocrafterConfig {
    public static SimpleConfig CONFIG;
    private static ModConfigProvider configs;

    public static final String TICK_SETTINGS = "tickSettings";
    public static final String NETWORK_SETTINGS = "networkSettings";

    public static int CRAFTING_TICKS;
    public static int COOLDOWN_TICKS;
    public static boolean SYNC_TICK_SETTINGS;

    public static void registerConfigs() {
        configs = new ModConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(AutocrafterMod.MOD_ID + "_config").provider(configs).request();

        assignConfigs();
    }

    private static void createConfigs() {
        configs.addComment("Tick settings (20 ticks = 1 second)");
        configs.addKeyValuePair(new Pair<>(TICK_SETTINGS + ".craftingTicks", 8), "int");
        configs.addKeyValuePair(new Pair<>(TICK_SETTINGS + ".cooldownTicks", 16), "int");

        configs.addComment("");
        configs.addComment("Network settings");
        configs.addComment("""
                Synchronize tick settings to players.
                Disabling this setting may cause visual glitches for other players in crafting progress.
                Other players may need to manually alter their tick settings to match the server host.""");
        configs.addKeyValuePair(new Pair<>(NETWORK_SETTINGS + ".synchronizeTickSettings", true), "bool");

    }

    private static void assignConfigs() {
        CRAFTING_TICKS = CONFIG.getOrDefault(TICK_SETTINGS + ".craftingTicks", 8);
        COOLDOWN_TICKS = CONFIG.getOrDefault(TICK_SETTINGS + ".cooldownTicks", 16);
        SYNC_TICK_SETTINGS = CONFIG.getOrDefault(NETWORK_SETTINGS + ".synchronizeTickSettings", true);

        AutocrafterMod.LOGGER.info("All " + configs.getConfigsList().size() + " have been set properly");
    }
}
