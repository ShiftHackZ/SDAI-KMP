# ONNX Runtime's JNI implementation requires its Java API names and members.
# https://onnxruntime.ai/docs/build/android.html
-keep class ai.onnxruntime.** { *; }

# JNI entry points and synchronous callback must retain their names.
-keep class com.shifthackz.aisdv1.feature.mnn.MnnNativeBridgeImpl { *; }
-keep class com.shifthackz.aisdv1.feature.mnn.MnnUpscalerNativeBridgeImpl { *; }

# RegisterNatives and FindClass use these literal binary names.
-keep class com.shifthackz.aisdv1.feature.sdxl.StableDiffusionCppNativeBridge { *; }
-keep interface com.shifthackz.aisdv1.feature.sdxl.StableDiffusionCppNativeBridge$ProgressCallback {
    public void onProgress(int, int);
}
-keep class com.shifthackz.aisdv1.feature.bonsai.BonsaiNativeBridge { *; }
-keep interface com.shifthackz.aisdv1.feature.bonsai.BonsaiNativeBridge$ProgressCallback {
    public void onProgress(int, int);
}
