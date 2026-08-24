package com.example.data.audio

enum class AudioType(val displayName: String) {
    ADHAN("صوت الأذان والتكبيرات"),
    DUA("أدعية وتسابيح صوتية"),
    REMINDER("نغمة تنبيه رقيقة")
}

data class CustomAudioItem(
    val id: String,
    val title: String,
    val description: String,
    val filePath: String? = null, // null for built-in or raw resource tones
    val rawResId: Int? = null, // Resource ID in res/raw for built-in audio
    val isBuiltIn: Boolean = true,
    val type: AudioType = AudioType.ADHAN,
    val durationFormatted: String = "0:30",
    val dateAddedMillis: Long = System.currentTimeMillis()
)
