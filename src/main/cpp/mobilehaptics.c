#include <jni.h>
#include <android/log.h>

#define LOG_TAG "MobileHaptics"

#define LOGE(...) \
    __android_log_print(
        ANDROID_LOG_ERROR,
        LOG_TAG,
        __VA_ARGS__
    )

static JavaVM *g_vm = NULL;

static jobject g_application = NULL;
static jobject g_vibrator = NULL;

static jclass g_vibration_effect_class = NULL;

static jmethodID g_get_system_service = NULL;
static jmethodID g_has_vibrator = NULL;
static jmethodID g_vibrate = NULL;
static jmethodID g_create_one_shot = NULL;

static int g_initialized = 0;

static void clear_exception(JNIEnv *env) {
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }
}

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void) reserved;

    g_vm = vm;

    return JNI_VERSION_1_6;
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
        LOGE("JNIEnv is NULL");
        return JNI_FALSE;
    }

    /*
     * Получаем Application напрямую из Android.
     * Это не зависит от DALVIK_APPLICATION.
     */
    jclass activity_thread =
            (*env)->FindClass(
                    env,
                    "android/app/ActivityThread"
            );

    if (activity_thread == NULL) {
        LOGE("ActivityThread class not found");
        clear_exception(env);
        return JNI_FALSE;
    }

    jmethodID current_application =
            (*env)->GetStaticMethodID(
                    env,
                    activity_thread,
                    "currentApplication",
                    "()Landroid/app/Application;"
            );

    if (current_application == NULL) {
        LOGE("currentApplication method not found");
        clear_exception(env);
        (*env)->DeleteLocalRef(
                env,
                activity_thread
        );
        return JNI_FALSE;
    }

    jobject application =
            (*env)->CallStaticObjectMethod(
                    env,
                    activity_thread,
                    current_application
            );

    if ((*env)->ExceptionCheck(env)) {
        LOGE("currentApplication call failed");
        clear_exception(env);

        (*env)->DeleteLocalRef(
                env,
                activity_thread
        );

        return JNI_FALSE;
    }

    (*env)->DeleteLocalRef(
            env,
            activity_thread
    );

    if (application == NULL) {
        LOGE("Android Application is NULL");
        return JNI_FALSE;
    }

    g_application =
            (*env)->NewGlobalRef(
                    env,
                    application
            );

    (*env)->DeleteLocalRef(
            env,
            application
    );

    if (g_application == NULL) {
        LOGE("Failed to create Application global reference");
        return JNI_FALSE;
    }

    jclass application_class =
            (*env)->GetObjectClass(
                    env,
                    g_application
            );

    if (application_class == NULL) {
        LOGE("Application class not found");
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
        LOGE("getSystemService not found");
        clear_exception(env);
        return JNI_FALSE;
    }

    jstring vibrator_service =
            (*env)->NewStringUTF(
                    env,
                    "vibrator"
            );

    if (vibrator_service == NULL) {
        LOGE("Failed to create vibrator service string");
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
        LOGE("getSystemService(vibrator) failed");
        clear_exception(env);
        return JNI_FALSE;
    }

    if (vibrator == NULL) {
        LOGE("Vibrator service is NULL");
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
        LOGE("Failed to create Vibrator global reference");
        return JNI_FALSE;
    }

    jclass vibrator_class =
            (*env)->GetObjectClass(
                    env,
                    g_vibrator
            );

    if (vibrator_class == NULL) {
        LOGE("Vibrator class not found");
        return JNI_FALSE;
    }

    /*
     * Проверяем, есть ли вибромотор.
     */
    g_has_vibrator =
            (*env)->GetMethodID(
                    env,
                    vibrator_class,
                    "hasVibrator",
                    "()Z"
            );

    if (g_has_vibrator == NULL) {
        LOGE("hasVibrator not found");
        clear_exception(env);
        return JNI_FALSE;
    }

    jboolean has_vibrator =
            (*env)->CallBooleanMethod(
                    env,
                    g_vibrator,
                    g_has_vibrator
            );

    if ((*env)->ExceptionCheck(env)) {
        LOGE("hasVibrator() failed");
        clear_exception(env);
        return JNI_FALSE;
    }

    if (!has_vibrator) {
        LOGE("Device reports no vibrator");
        return JNI_FALSE;
    }

    /*
     * Android 8.0+ API.
     */
    g_vibrate =
            (*env)->GetMethodID(
                    env,
                    vibrator_class,
                    "vibrate",
                    "(Landroid/os/VibrationEffect;)V"
            );

    if (g_vibrate == NULL) {
        LOGE("Vibrator.vibrate(VibrationEffect) not found");
        clear_exception(env);
        return JNI_FALSE;
    }

    jclass effect_class =
            (*env)->FindClass(
                    env,
                    "android/os/VibrationEffect"
            );

    if (effect_class == NULL) {
        LOGE("VibrationEffect class not found");
        clear_exception(env);
        return JNI_FALSE;
    }

    g_vibration_effect_class =
            (*env)->NewGlobalRef(
                    env,
                    effect_class
            );

    (*env)->DeleteLocalRef(
            env,
            effect_class
    );

    if (g_vibration_effect_class == NULL) {
        LOGE("Failed to create VibrationEffect global reference");
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
        LOGE("VibrationEffect.createOneShot not found");
        clear_exception(env);
        return JNI_FALSE;
    }

    g_initialized = 1;

    LOGE("Mobile Haptics native vibration initialized");

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
        LOGE("createOneShot failed");
        clear_exception(env);
        return;
    }

    if (effect == NULL) {
        LOGE("VibrationEffect is NULL");
        return;
    }

    (*env)->CallVoidMethod(
            env,
            g_vibrator,
            g_vibrate,
            effect
    );

    if ((*env)->ExceptionCheck(env)) {
        LOGE("Vibrator.vibrate() failed");
        clear_exception(env);
    }

    (*env)->DeleteLocalRef(
            env,
            effect
    );
}