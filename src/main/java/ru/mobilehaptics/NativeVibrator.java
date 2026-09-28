package ru.mobilehaptics;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class NativeVibrator {

    private static boolean loaded = false;

    private NativeVibrator() {
    }

    public static synchronized void init() {
        if (loaded) {
            return;
        }

        /*
         * На Android через Zalith Launcher
         * должен присутствовать DALVIK_APPLICATION.
         */
        if (System.getenv("DALVIK_APPLICATION") == null) {
            return;
        }

        String architecture =
                System.getProperty("os.arch", "").toLowerCase();

        String resourcePath;

        if (architecture.contains("aarch64")
                || architecture.contains("arm64")) {

            resourcePath =
                    "/native/arm64-v8a/libmobilehaptics.so";

        } else if (architecture.contains("x86_64")
                || architecture.contains("amd64")) {

            resourcePath =
                    "/native/x86_64/libmobilehaptics.so";

        } else {
            return;
        }

        try {
            Path nativeDirectory =
                    FabricLoader.getInstance()
                            .getConfigDir()
                            .resolve("mobile-haptics-native");

            Files.createDirectories(nativeDirectory);

            Path library =
                    nativeDirectory.resolve("libmobilehaptics.so");

            try (InputStream input =
                         NativeVibrator.class.getResourceAsStream(resourcePath)) {

                if (input == null) {
                    return;
                }

                Files.copy(
                        input,
                        library,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            System.load(
                    library.toAbsolutePath().toString()
            );

            /*
             * После загрузки .so получаем Android Vibrator.
             */
            loaded = nativeInit();

        } catch (Throwable ignored) {
            loaded = false;
        }
    }

    public static void vibrate(
            int durationMs,
            int strengthPercent
    ) {
        if (!loaded) {
            init();
        }

        if (!loaded) {
            return;
        }

        int duration =
                Math.max(
                        1,
                        Math.min(durationMs, 5000)
                );

        int strength =
                Math.max(
                        1,
                        Math.min(strengthPercent, 100)
                );

        try {
            nativeVibrate(
                    duration,
                    strength
            );
        } catch (Throwable ignored) {
        }
    }

    private static native boolean nativeInit();

    private static native void nativeVibrate(
            int durationMs,
            int strengthPercent
    );
}