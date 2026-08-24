# Add project specific ProGuard rules here.
# Keep Room entities, DAOs and Database
-keep class androidx.room.** { *; }
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep Moshi models and adapters
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep class com.squareup.moshi.** { *; }
-keep class * extends com.squareup.moshi.JsonAdapter { *; }

# Keep App Widget Providers & Services
-keep class * extends android.appwidget.AppWidgetProvider { *; }
-keep class * extends android.widget.RemoteViewsService { *; }
-keep class * extends android.content.BroadcastReceiver { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Data Models
-keep class com.example.data.model.** { *; }
-keep class com.example.data.quran.** { *; }
-keep class com.example.data.prayer.** { *; }
-keep class com.example.data.audio.** { *; }
-keep class com.example.data.ai.** { *; }

# Preserve line numbers for release diagnostics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

