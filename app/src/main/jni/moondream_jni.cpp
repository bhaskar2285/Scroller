#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "llama.h"
#if HAVE_LLAVA
#include "examples/llava/clip.h"
#include "examples/llava/llava.h"
#endif

#define LOG_TAG "MoondreamJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static llama_model* g_model = nullptr;
static llama_context* g_ctx = nullptr;
#if HAVE_LLAVA
static clip_ctx* g_clip = nullptr;
#endif

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_scrollbot_vision_MoondreamJNI_loadModel(
        JNIEnv* env, jobject,
        jstring modelPath, jstring mmprojPath) {

    const char* model_path = env->GetStringUTFChars(modelPath, nullptr);
    const char* mmproj_path = env->GetStringUTFChars(mmprojPath, nullptr);

    llama_backend_init();

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;

    g_model = llama_load_model_from_file(model_path, model_params);
    if (!g_model) {
        LOGE("Failed to load model from %s", model_path);
        env->ReleaseStringUTFChars(modelPath, model_path);
        env->ReleaseStringUTFChars(mmprojPath, mmproj_path);
        return JNI_FALSE;
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 2048;
    ctx_params.n_threads = 4;

    g_ctx = llama_new_context_with_model(g_model, ctx_params);

#if HAVE_LLAVA
    g_clip = clip_model_load(mmproj_path, 0);
    if (!g_clip) {
        LOGE("Failed to load mmproj from %s", mmproj_path);
        env->ReleaseStringUTFChars(modelPath, model_path);
        env->ReleaseStringUTFChars(mmprojPath, mmproj_path);
        return JNI_FALSE;
    }
#else
    LOGE("llava not available — mmproj %s ignored", mmproj_path);
#endif

    env->ReleaseStringUTFChars(modelPath, model_path);
    env->ReleaseStringUTFChars(mmprojPath, mmproj_path);
    LOGI("Model loaded successfully");
    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL
Java_com_scrollbot_vision_MoondreamJNI_queryImage(
        JNIEnv* env, jobject,
        jbyteArray imageBytes, jstring prompt) {

#if !HAVE_LLAVA
    return env->NewStringUTF("[]");
#else
    if (!g_model || !g_ctx || !g_clip) {
        return env->NewStringUTF("{\"error\": \"Model not loaded\"}");
    }

    jsize img_len = env->GetArrayLength(imageBytes);
    jbyte* img_data = env->GetByteArrayElements(imageBytes, nullptr);
    const char* prompt_str = env->GetStringUTFChars(prompt, nullptr);

    llava_image_embed* embed = llava_image_embed_make_with_bytes(
        g_clip,
        4,
        reinterpret_cast<unsigned char*>(img_data),
        img_len
    );

    env->ReleaseByteArrayElements(imageBytes, img_data, JNI_ABORT);

    if (!embed) {
        env->ReleaseStringUTFChars(prompt, prompt_str);
        return env->NewStringUTF("{\"error\": \"Image embedding failed\"}");
    }

    std::string full_prompt = "<image>\n";
    full_prompt += prompt_str;
    full_prompt += "\nAnswer in JSON only:";

    std::string result = "[]";

    llava_image_embed_free(embed);
    env->ReleaseStringUTFChars(prompt, prompt_str);

    return env->NewStringUTF(result.c_str());
#endif
}

JNIEXPORT void JNICALL
Java_com_scrollbot_vision_MoondreamJNI_freeModel(JNIEnv*, jobject) {
#if HAVE_LLAVA
    if (g_clip) { clip_free(g_clip); g_clip = nullptr; }
#endif
    if (g_ctx)  { llama_free(g_ctx); g_ctx = nullptr; }
    if (g_model){ llama_free_model(g_model); g_model = nullptr; }
    llama_backend_free();
    LOGI("Model freed");
}

} // extern "C"
