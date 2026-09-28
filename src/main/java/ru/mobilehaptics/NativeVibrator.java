package ru.mobilehaptics;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NativeVibrator {
    private static final String LIB_NAME = "mobilehaptics_bridge";
    private static boolean loaded = false;

    private NativeVibrator() {
    }

    public static void init() {
        if (loaded) {
            return;
        }

        // Zalith Launcher exposes the Android application through this
        // environment variable when Minecraft is running inside Android.
        if (System.getenv("DALVIK_APPLICATION") == null) {
            return;
        }

        String arch = System.getProperty("os.arch", "").toLowerCase();

        String nativePath;

        if (arch.contains("aarch64") || arch.contains("arm64")) {
            nativePath = "native/arm64-v8a/libmobilehaptics.so";
        } else if (arch.contains("x86_64") || arch.contains("amd64")) {
            nativePath = "native/x86_64/libmobilehaptics.so";
        } else {
            return;
        }

        try {
            Path output = FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve(LIB_NAME + ".so");

            try (InputStream input = NativeVibrator.class
                    .getClassLoader()
                    .getResourceAsStream(nativePath)) {

                if (input == null) {
                    return;
                }

                Files.copy(input, output,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            System.load(output.toAbsolutePath().toString());
            loaded = true;

        } catch (IOException | UnsatisfiedLinkError ignored) {
            // Android vibration is optional. Never crash Minecraft if it cannot load.
        }
    }

    public static void vibrate(int durationMs, int strengthPercent) {
        if (!loaded) {
            init();
        }

        if (!loaded) {
            return;
        }

        int duration = Math.max(1, Math.min(durationMs, 5000));
        int strength = Math.max(1, Math.min(strengthPercent, 100));

        nativeVibrate(duration, strength);
    }

    private static native void nativeVibrate(int durationMs, int strengthPercent);
}