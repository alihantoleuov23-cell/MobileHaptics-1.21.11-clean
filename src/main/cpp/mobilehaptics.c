#include <jni.h>
#include <stdlib.h>

static JavaVM *g_vm = NULL;
static jobject g_application = NULL;
static jobject g_vibrator = NULL;

static jmethodID g_get_system_service = NULL;
static jmethodID g_vibrate = NULL;
static jmethodID g_has_vibrator = NULL;

static jclass g_vibration_effect_class = NULL;
static jmethodID g_create_one_shot = NULL;
static jmethodID g_vibrate_effect = NULL;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void) reserved;

    g_vm = vm;

    return JNI_VERSION_1_6;
}

static void clear_exception(JNIEnv *env) {
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionClear(env);
    }
}

JNIEXPORT void JNICALL
Java_ru_mobilehaptics_NativeVibrator_nativeVibrate(
        JNIEnv *env,
        jclass clazz,
        jint durationMs,
        jint strengthPercent
) {
    (void) clazz;

    if (g_vibrator == NULL) {
        return;
    }

    if (durationMs < 1) {
        durationMs = 1;
    }

    if (durationMs > 5000) {
        durationMs = 5000;
    }

    if (strengthPercent < 1) {
        strengthPercent = 1;
    }

    if (strengthPercent > 100) {
        strengthPercent = 100;
    }

    /*
     * Android VibrationEffect amplitude:
     *
     * 1   = minimum
     * 255 = maximum
     */
    jint amplitude =
            (jint)((strengthPercent * 255L) / 100L);

    if (g_vibration_effect_class != NULL &&
        g_create_one_shot != NULL &&
        g_vibrate_effect != NULL) {

        jobject effect =
                (*env)->CallStaticObjectMethod(
                        env,
                        g_vibration_effect_class,
                        g_create_one_shot,
                        (jlong) durationMs,
                        amplitude
                );

        if ((*env)->ExceptionCheck(env)) {
            clear_exception(env);
            return;
        }

        if (effect != NULL) {
            (*env)->CallVoidMethod(
                    env,
                    g_vibrator,
                    g_vibrate_effect,
                    effect
            );

            (*env)->DeleteLocalRef(env, effect);
        }

        clear_exception(env);
        return;
    }

    /*
     * Fallback for older Android implementations.
     * This does not allow amplitude control, but vibration
     * itself can still work.
     */
    if (g_vibrate != NULL) {
        (*env)->CallVoidMethod(
                env,
                g_vibrator,
                g_vibrate,
                (jlong) durationMs
        );

        clear_exception(env);
    }
}