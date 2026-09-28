package ru.mobilehaptics;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class NativeVibrator {

    private static boolean loaded = false;

    private static Object vibrator;

    private static Method createOneShotMethod;
    private static Method vibrateMethod;

    private NativeVibrator() {
    }

    public static synchronized void init() {

        if (loaded) {
            return;
        }

        try {

            /*
             * Получаем настоящее Android Application
             * внутри процесса Zalith/Pojav.
             */
            Class<?> activityThreadClass =
                    Class.forName(
                            "android.app.ActivityThread"
                    );

            Method currentApplicationMethod =
                    activityThreadClass.getMethod(
                            "currentApplication"
                    );

            Object application =
                    currentApplicationMethod.invoke(
                            null
                    );

            if (application == null) {
                return;
            }

            /*
             * Получаем системный Vibrator.
             */
            Method getSystemServiceMethod =
                    application.getClass().getMethod(
                            "getSystemService",
                            String.class
                    );

            vibrator =
                    getSystemServiceMethod.invoke(
                            application,
                            "vibrator"
                    );

            if (vibrator == null) {
                return;
            }

            Class<?> vibratorClass =
                    Class.forName(
                            "android.os.Vibrator"
                    );

            /*
             * Проверяем, есть ли вибромотор.
             */
            Method hasVibratorMethod =
                    vibratorClass.getMethod(
                            "hasVibrator"
                    );

            Object hasVibrator =
                    hasVibratorMethod.invoke(
                            vibrator
                    );

            if (!Boolean.TRUE.equals(
                    hasVibrator
            )) {
                return;
            }

            /*
             * Android VibrationEffect.
             */
            Class<?> vibrationEffectClass =
                    Class.forName(
                            "android.os.VibrationEffect"
                    );

            createOneShotMethod =
                    vibrationEffectClass.getMethod(
                            "createOneShot",
                            long.class,
                            int.class
                    );

            vibrateMethod =
                    vibratorClass.getMethod(
                            "vibrate",
                            vibrationEffectClass
                    );

            loaded = true;

            System.err.println(
                    "[MobileHaptics] Android vibrator initialized"
            );

        } catch (Throwable throwable) {

            loaded = false;

            System.err.println(
                    "[MobileHaptics] Failed to initialize Android vibrator: "
                            + throwable
            );
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
                        Math.min(
                                durationMs,
                                5000
                        )
                );

        int strength =
                Math.max(
                        1,
                        Math.min(
                                strengthPercent,
                                100
                        )
                );

        /*
         * Android amplitude:
         * 1   = minimum
         * 255 = maximum
         */
        int amplitude =
                Math.max(
                        1,
                        Math.min(
                                255,
                                Math.round(
                                        strength * 2.55f
                                )
                        )
                );

        try {

            Object effect =
                    createOneShotMethod.invoke(
                            null,
                            (long) duration,
                            amplitude
                    );

            vibrateMethod.invoke(
                    vibrator,
                    effect
            );

        } catch (Throwable throwable) {

            System.err.println(
                    "[MobileHaptics] Vibration failed: "
                            + throwable
            );
        }
    }
}