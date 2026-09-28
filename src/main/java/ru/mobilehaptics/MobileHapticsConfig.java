package ru.mobilehaptics;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class MobileHapticsConfig {
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("mobile-haptics.properties");

    public boolean enabled = true;
    public boolean breakEnabled = true;
    public boolean placeEnabled = true;

    public int breakDuration = 35;
    public int placeDuration = 20;

    /** Percentage 1..100. Converted to Android amplitude 1..255 when vibrating. */
    public int breakStrength = 70;
    public int placeStrength = 50;

    public static MobileHapticsConfig load() {
        MobileHapticsConfig config = new MobileHapticsConfig();

        if (!Files.exists(FILE)) {
            config.save();
            return config;
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            properties.load(in);

            config.enabled = readBoolean(properties, "enabled", config.enabled);
            config.breakEnabled = readBoolean(properties, "breakEnabled", config.breakEnabled);
            config.placeEnabled = readBoolean(properties, "placeEnabled", config.placeEnabled);

            config.breakDuration = readInt(properties, "breakDuration", config.breakDuration, 5, 200);
            config.placeDuration = readInt(properties, "placeDuration", config.placeDuration, 5, 200);

            config.breakStrength = readInt(properties, "breakStrength", config.breakStrength, 1, 100);
            config.placeStrength = readInt(properties, "placeStrength", config.placeStrength, 1, 100);
        } catch (IOException ignored) {
        }

        return config;
    }

    public void save() {
        Properties properties = new Properties();
        properties.setProperty("enabled", Boolean.toString(enabled));
        properties.setProperty("breakEnabled", Boolean.toString(breakEnabled));
        properties.setProperty("placeEnabled", Boolean.toString(placeEnabled));
        properties.setProperty("breakDuration", Integer.toString(breakDuration));
        properties.setProperty("placeDuration", Integer.toString(placeDuration));
        properties.setProperty("breakStrength", Integer.toString(breakStrength));
        properties.setProperty("placeStrength", Integer.toString(placeStrength));

        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) {
                properties.store(out, "Mobile Haptics settings");
            }
        } catch (IOException ignored) {
        }
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    private static int readInt(Properties properties, String key, int fallback, int min, int max) {
        try {
            return Math.max(min, Math.min(max,
                    Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)))));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
