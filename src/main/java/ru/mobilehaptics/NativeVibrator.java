package ru.mobilehaptics;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import net.fabricmc.loader.api.FabricLoader;

public final class NativeVibrator {

    private static final String LIBRARY_NAME = "mobilehaptics";

    private static volatile boolean initialized = false;
    private static volatile boolean available = false;

    private NativeVibrator() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }

        initialized = true;

        try {
            loadNativeLibrary();

            available = nativeIsAvailable();

            System.out.println(
                    "[MobileHaptics] Native vibrator available: "
                            + available
            );

        } catch (Throwable t) {
            available = false;

            System.err.println(
                    "[MobileHaptics] Failed to initialize native vibrator:"
            );

            t.printStackTrace();
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static void vibrate(
            int durationMs,
            int strengthPercent
    ) {
        if (!initialized) {
            init();
        }

        if (!available) {
            return;
        }

        durationMs = Math.max(
                1,
                Math.min(durationMs, 2000)
        );

        strengthPercent = Math.max(
                0,
                Math.min(strengthPercent, 100)
        );

        if (strengthPercent <= 0) {
            return;
        }

        try {
            nativeVibrate(
                    durationMs,
                    strengthPercent
            );
        } catch (Throwable t) {
            System.err.println(
                    "[MobileHaptics] Native vibration failed:"
            );

            t.printStackTrace();
        }
    }

    private static void loadNativeLibrary()
            throws IOException {

        String resourcePath = getNativeResourcePath();

        String fileName =
                "lib" + LIBRARY_NAME + ".so";

        Path directory =
                FabricLoader.getInstance()
                        .getConfigDir()
                        .resolve("mobile-haptics-native");

        Files.createDirectories(directory);

        Path output =
                directory.resolve(fileName);

        try (
                InputStream input =
                        NativeVibrator.class
                                .getResourceAsStream(resourcePath)
        ) {
            if (input == null) {
                throw new IOException(
                        "Native library resource not found: "
                                + resourcePath
                );
            }

            boolean shouldCopy =
                    !Files.exists(output)
                            || Files.size(output) == 0;

            if (shouldCopy) {
                try (
                        OutputStream out =
                                Files.newOutputStream(output)
                ) {
                    input.transferTo(out);
                }
            } else {
                /*
                 * The library may have been replaced by a
                 * newer build. Make sure the resource can be
                 * copied again when necessary.
                 */
                Path temporary =
                        directory.resolve(
                                fileName + ".tmp"
                        );

                Files.copy(
                        input,
                        temporary,
                        StandardCopyOption.REPLACE_EXISTING
                );

                Files.move(
                        temporary,
                        output,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        }

        System.load(
                output.toAbsolutePath().toString()
        );

        System.out.println(
                "[MobileHaptics] Native library loaded: "
                        + output
        );
    }

    private static String getNativeResourcePath() {
        String arch =
                System.getProperty(
                        "os.arch",
                        ""
                ).toLowerCase();

        if (
                arch.contains("aarch64")
                        || arch.contains("arm64")
        ) {
            return "/native/arm64-v8a/libmobilehaptics.so";
        }

        if (
                arch.contains("amd64")
                        || arch.contains("x86_64")
                        || arch.contains("x86-64")
        ) {
            return "/native/x86_64/libmobilehaptics.so";
        }

        throw new IllegalStateException(
                "Unsupported CPU architecture: "
                        + arch
        );
    }

    private static native boolean nativeIsAvailable();

    private static native void nativeVibrate(
            int durationMs,
            int strengthPercent
    );
}