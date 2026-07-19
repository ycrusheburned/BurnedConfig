package com.kizilmc.burnedconfig;

import net.fabricmc.api.ModInitializer;

import java.util.logging.Logger;

public final class BurnedConfigMod implements ModInitializer {

    private static final Logger LOGGER = Logger.getLogger("BurnedConfig");

    @Override
    public void onInitialize() {
        // Registering here just proves the library initializes cleanly stand-alone.
        // Other mods depend on BurnedConfig and call ConfigManager.register(YourConfig.class)
        // from their own onInitialize().
        ExampleConfig example = ConfigManager.register(ExampleConfig.class);
        LOGGER.info("BurnedConfig loaded. Example config welcomeMessage=" + example.welcomeMessage);
    }

    @Config(name = "burnedconfig_example")
    public static final class ExampleConfig {
        @ConfigEntry(comment = "Shown once in the log to prove config read/write works")
        public String welcomeMessage = "KIZILMC'ye hosgeldin!";

        @ConfigEntry(comment = "Just a bounded number to demonstrate clamping", min = 0, max = 100)
        public int exampleTier = 10;

        public boolean exampleFlag = true;
    }
}
