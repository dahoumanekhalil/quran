# --------------------------------------------------------------------
# Mushaf — R8 / Proguard rules (release build only)
# CI currently builds :app:assembleDebug so these are dormant until the
# Phase 17 release candidate. Keep them here so R8 works out of the box
# when we do build release.
# --------------------------------------------------------------------

# Keep all app classes. Small app, no obfuscation win worth the risk.
-keep class app.mushaf.** { *; }

# Attributes needed by kotlinx-serialization + reflection-based DI.
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions

# ---- kotlinx.serialization -----------------------------------------
# Serializers are generated in the same package as @Serializable classes.
# The `-keep class app.mushaf.** { *; }` above already covers our own
# serializers; this block keeps the framework's internals.
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class **$$serializer { *; }
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Hilt / Dagger -------------------------------------------------
# Hilt-generated code lives outside our package. Its own consumer rules
# handle most of it; keep constructors used for injection.
-keep,allowobfuscation @interface dagger.hilt.**
-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }
-keep class hilt_aggregated_deps.** { *; }

# ---- Compose -------------------------------------------------------
# Compose ships its own consumer rules via the AndroidX BOM. Nothing
# extra needed here unless we start using reflection APIs.

# ---- SQLite AndroidX ------------------------------------------------
# We use android.database.sqlite.SQLiteDatabase directly — framework
# classes are always kept; nothing needed here.
