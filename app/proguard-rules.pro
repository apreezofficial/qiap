# Qiap R8 rules. Keep this file short; add a comment with the reason for every rule.

# MediaPipe Tasks ships no consumer rules. Its native code looks up Java classes and methods
# by name over JNI (result listeners, AutoValue results), so R8 must not rename or remove them.
-keep class com.google.mediapipe.** { *; }
# MediaPipe passes graph configs as protobuf-lite messages, which are read reflectively.
-keep class com.google.protobuf.** { *; }

# Compile-time-only annotations and AutoValue/Guava internals referenced by MediaPipe but never
# loaded at runtime.
-dontwarn com.google.auto.value.**
-dontwarn javax.annotation.**
-dontwarn javax.lang.model.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn org.checkerframework.**
-dontwarn com.google.mediapipe.proto.**
