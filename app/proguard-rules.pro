# VisionPlayer ProGuard 配置
# 更新日期: 2025-11-30
# 用途: APK瘦身优化 - 代码混淆和压缩

# ================================
# 基础配置
# ================================
# 保留行号信息，便于追踪崩溃
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 保留泛型签名
-keepattributes Signature
# 保留注解
-keepattributes *Annotation*
# 保留异常信息
-keepattributes Exceptions

# ================================
# Native 方法保护（JNI）
# ================================
# 保留所有 native 方法（C++调用需要）
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留 NativePlayer 类的所有内容（JNI桥接类）
-keep class com.live.visionplay.NativePlayer {
    *;
}

# 保留所有被 @Keep 注解的类和成员
-keep @androidx.annotation.Keep class *
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# ================================
# ViewModel 保护
# ================================
# 保留 ViewModel 类（反射创建）
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# 保留 ViewModel 的工厂方法
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    public <init>(android.app.Application);
}

# ================================
# 数据类保护
# ================================
# 保留所有数据类（用于序列化和反序列化）
-keep class com.live.visionplay.model.** { *; }

# 保留 StateFlow 的数据类
-keepclassmembers class * {
    kotlinx.coroutines.flow.StateFlow *;
}

# ================================
# Kotlin 相关
# ================================
# 保留 Kotlin 元数据
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# 保留 Kotlin 协程
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# 保留 Kotlin 内部类
-keep class kotlin.Metadata { *; }

# ================================
# ViewBinding 保护
# ================================
# 保留 ViewBinding 类
-keep class * implements androidx.viewbinding.ViewBinding {
    *;
}

# 保留自动生成的 ViewBinding
-keep class com.live.visionplay.databinding.** { *; }

# ================================
# 翻译服务保护
# ================================
# 保留翻译相关类（MLKit 需要）
-keep class com.live.visionplay.translation.** { *; }

# Google MLKit Translation
-keep class com.google.mlkit.** { *; }
-keep interface com.google.mlkit.** { *; }

# ================================
# Oboe 音频库保护
# ================================
# 保留 Oboe 相关的 native 方法
-keep class com.google.oboe.** { *; }

# ================================
# 第三方库配置
# ================================
# AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**

# Material Design
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ================================
# 反射和序列化
# ================================
# 保留 Serializable 类
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ================================
# 枚举类保护
# ================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ================================
# 崩溃报告
# ================================
# 移除日志（Release版本）
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# 保留错误和警告日志
-assumenosideeffects class android.util.Log {
    public static *** w(...);
    public static *** e(...);
}

# ================================
# 优化配置
# ================================
# 允许代码优化
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# 不跳过非公共的库的类
-dontskipnonpubliclibraryclasses

# 混淆时不生成大小写混合的类名
-dontusemixedcaseclassnames

# ================================
# WebView 相关（如果使用）
# ================================
# 如果项目使用 WebView，取消下面的注释
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# ================================
# 警告处理
# ================================
# 忽略警告（仅在确认安全的情况下使用）
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ================================
# 自定义规则
# ================================
# 如果遇到混淆问题，在这里添加额外的规则
