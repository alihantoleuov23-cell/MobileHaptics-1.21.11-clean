#include <jni.h>
#include <stdint.h>
#include <stdlib.h>

static JavaVM *g_vm = NULL;

static jobject g_application = NULL;
static jobject g_vibrator = NULL;

static jmethodID g_get_system_service = NULL;
static jmethodID g_vibrate = NULL;
static jmethodID g_has_vibrator = NULL;

static jclass g_vibration_effect_class = NULL;
static jmethodID g_create_one_shot = NULL;

static int g_initialized = 0;

static void clear_exception(JNIEnv *env) {
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionClear(env);
    }
}

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void) reserved;

    g_vm = vm;

    return JNI_VERSION_1_6;
}

static JNIEnv *get_env(void) {
    JNIEnv *env = NULL;

    if (g_vm == NULL) {
        return NULL;
    }

    jint result =
            (*g_vm)->GetEnv(
                    g_vm,
                    (void **) &env,
                    JNI_VERSION_1_6
            );

    if (result == JNI_OK) {
        return env;
    }

    if (result == JNI_EDETACHED) {
        if ((*g_vm)->AttachCurrentThread(
                g_vm,
                &env,
                NULL
        ) != JNI_OK) {
            return NULL;
        }

        return env;
    }

    return NULL;
}

static jobject get_application_from_environment(
        JNIEnv *env
) {
    const char *value =
            getenv("DALVIK_APPLICATION");

    if (value == NULL || value[0] == '\0') {
        return NULL;
    }

    uintptr_t address =
            (uintptr_t) strtoull(
                    value,
                    NULL,
                    0
            );

    if (address == 0) {
        return NULL;
    }

    jobject application =
            (jobject) address;

    return (*env)->NewGlobalRef(
            env,
            application
    );
}

JNIEXPORT jboolean JNICALL
Java_ru_mobilehaptics_NativeVibrator_nativeInit(
        JNIEnv *env,
        jclass clazz
) {
    (void) clazz;

    if (g_initialized) {
        return JNI_TRUE;
    }

    if (env == NULL) {
        return JNI_FALSE;
    }

    jobject application =
            get_application_from_environment(env);

    if (application == NULL) {
        return JNI_FALSE;
    }

    g_application = application;

    jclass application_class =
            (*env)->GetObjectClass(
                    env,
                    g_application
            );

    if (application_class == NULL) {
        return JNI_FALSE;
    }

    g_get_system_service =
            (*env)->GetMethodID(
                    env,
                    application_class,
                    "getSystemService",
                    "(Ljava/lang/String;)Ljava/lang/Object;"
            );

    if (g_get_system_service == NULL) {
        clear_exception(env);
        return JNI_FALSE;
    }

    jstring vibrator_service =
            (*env)->NewStringUTF(
                    env,
                    "vibrator"
            );

    if (vibrator_service == NULL) {
        return JNI_FALSE;
    }

    jobject vibrator =
            (*env)->CallObjectMethod(
                    env,
                    g_application,
                    g_get_system_service,
                    vibrator_service
            );

    (*env)->DeleteLocalRef(
            env,
            vibrator_service
    );

    if ((*env)->ExceptionCheck(env)) {
        clear_exception(env);
        return JNI_FALSE;
    }

    if (vibrator == NULL) {
        return JNI_FALSE;
    }

    g_vibrator =
            (*env)->NewGlobalRef(
                    env,
                    vibrator
            );

    (*env)->DeleteLocalRef(
            env,
            vibrator
    );

    if (g_vibrator == NULL) {
        return JNI_FALSE;
    }

    jclass vibrator_class =
            (*env)->GetObjectClass(
                    env,
                    g_vibrator
            );

    if (vibrator_class == NULL) {
        return JNI_FALSE;
    }

    g_has_vibrator =
            (*env)->GetMethodID(
                    env,
                    vibrator_class,
                    "hasVibrator",
                    "()Z"
            );

    if (g_has_vibrator != NULL) {
        jboolean has_vibrator =
                (*env)->CallBooleanMethod(
                        env,
                        g_vibrator,
                        g_has_vibrator
                );

        if ((*env)->ExceptionCheck(env)) {
            clear_exception(env);
            return JNI_FALSE;
        }

        if (!has_vibrator) {
            return JNI_FALSE;
        }
    }

    g_vibrate =
            (*env)->GetMethodID(
                    env,
                    vibrator_class,
                    "vibrate",
                    "(Landroid/os/VibrationEffect;)V"
            );

    if (g_vibrate == NULL) {
        clear_exception(env);
        return JNI_FALSE;
    }

    jclass local_effect_class =
            (*env)->FindClass(
                    env,
                    "android/os/VibrationEffect"
            );

    if (local_effect_class == NULL) {
        clear_exception(env);
        return JNI_FALSE;
    }

    g_vibration_effect_class =
            (*env)->NewGlobalRef(
                    env,
                    local_effect_class
            );

    (*env)->DeleteLocalRef(
            env,
            local_effect_class
    );

    if (g_vibration_effect_class == NULL) {
        return JNI_FALSE;
    }

    g_create_one_shot =
            (*env)->GetStaticMethodID(
                    env,
                    g_vibration_effect_class,
                    "createOneShot",
                    "(JI)Landroid/os/VibrationEffect;"
            );

    if (g_create_one_shot == NULL) {
        clear_exception(env);
        return JNI_FALSE;
    }

    g_initialized = 1;

    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_ru_mobilehaptics_NativeVibrator_nativeVibrate(
        JNIEnv *env,
        jclass clazz,
        jint durationMs,
        jint strengthPercent
) {
    (void) clazz;

    if (!g_initialized) {
        return;
    }

    if (env == NULL) {
        env = get_env();
    }

    if (env == NULL) {
        return;
    }

    if (g_vibrator == NULL ||
        g_vibration_effect_class == NULL ||
        g_create_one_shot == NULL ||
        g_vibrate == NULL) {
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

    jint amplitude =
            (strengthPercent * 255) / 100;

    if (amplitude < 1) {
        amplitude = 1;
    }

    if (amplitude > 255) {
        amplitude = 255;
    }

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

    if (effect == NULL) {
        return;
    }

    (*env)->CallVoidMethod(
            env,
            g_vibrator,
            g_vibrate,
            effect
    );

    if ((*env)->ExceptionCheck(env)) {
        clear_exception(env);
    }

    (*env)->DeleteLocalRef(
            env,
            effect
    );
}