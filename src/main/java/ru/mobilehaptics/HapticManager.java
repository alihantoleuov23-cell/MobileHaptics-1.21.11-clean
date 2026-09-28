package ru.mobilehaptics;

public final class HapticManager {
    private static final MobileHapticsConfig CONFIG = MobileHapticsConfig.load();

    private HapticManager() {
    }

    public static MobileHapticsConfig getConfig() {
        return CONFIG;
    }

    public static void save() {
        CONFIG.save();
    }

    public static void breakBlock() {
        if (!CONFIG.enabled || !CONFIG.breakEnabled) {
            return;
        }

        vibrate(CONFIG.breakDuration, CONFIG.breakStrength);
    }

    public static void placeBlock() {
        if (!CONFIG.enabled || !CONFIG.placeEnabled) {
            return;
        }

        vibrate(CONFIG.placeDuration, CONFIG.placeStrength);
    }

    public static void test() {
        if (!CONFIG.enabled) {
            return;
        }

        vibrate(CONFIG.breakDuration, CONFIG.breakStrength);
    }

    private static void vibrate(int duration, int strength) {
        try {
            NativeVibrator.vibrate(duration, strength);
        } catch (Throwable ignored) {
            // The mod should never crash Minecraft if Android haptics are unavailable.
        }
    }
}
