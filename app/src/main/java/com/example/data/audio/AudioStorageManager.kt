package com.example.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object AudioStorageManager {
    private const val PREFS_NAME = "custom_audio_prefs"
    private const val KEY_AUDIO_LIST = "saved_audio_list_json"
    private const val KEY_SELECTED_ADHAN = "selected_adhan_id"
    private const val KEY_SELECTED_DUA = "selected_dua_id"
    private const val KEY_PRAYER_NOTIFICATIONS_ENABLED = "prayer_notifications_enabled"
    private const val KEY_PRE_PRAYER_ALERT_ENABLED = "pre_prayer_alert_enabled"

    private var mediaPlayer: MediaPlayer? = null
    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    val BUILT_IN_AUDIOS = listOf(
        CustomAudioItem(
            id = "builtin_default_adhan",
            title = "الأذان الافتراضي الثابت (صوت ندي خاشع)",
            description = "الله أكبر، الله أكبر، أشهد أن لا إله إلا الله، حي على الصلاة",
            rawResId = com.example.R.raw.adhan_default,
            isBuiltIn = true,
            type = AudioType.ADHAN,
            durationFormatted = "1:16"
        ),
        CustomAudioItem(
            id = "builtin_makkah",
            title = "أذان الحرم المكي الشريف",
            description = "صوت الأذان المبارك بنبرة مكية مهيبة",
            rawResId = com.example.R.raw.adhan_makkah,
            isBuiltIn = true,
            type = AudioType.ADHAN,
            durationFormatted = "0:32"
        ),
        CustomAudioItem(
            id = "builtin_madinah",
            title = "أذان المسجد النبوي الشريف",
            description = "صوت شجي يبعث على السكينة والخشوع في القلوب",
            rawResId = com.example.R.raw.adhan_madinah,
            isBuiltIn = true,
            type = AudioType.ADHAN,
            durationFormatted = "0:28"
        ),
        CustomAudioItem(
            id = "builtin_takbeerat",
            title = "تكبيرات الصلاة والأعياد",
            description = "الله أكبر، الله أكبر، لا إله إلا الله",
            rawResId = com.example.R.raw.takbeerat,
            isBuiltIn = true,
            type = AudioType.ADHAN,
            durationFormatted = "0:18"
        ),
        CustomAudioItem(
            id = "builtin_dua_after_adhan",
            title = "دعاء ما بعد الأذان (الوسيلة والفضيلة)",
            description = "اللهم رب هذه الدعوة التامة والصلاة القائمة",
            rawResId = com.example.R.raw.dua_after_adhan,
            isBuiltIn = true,
            type = AudioType.DUA,
            durationFormatted = "0:16"
        ),
        CustomAudioItem(
            id = "builtin_soft_chime",
            title = "نغمة تنبيه ناعمة وهادئة",
            description = "رنين إسلامي هادئ مناسب لأوقات العمل",
            rawResId = com.example.R.raw.soft_chime,
            isBuiltIn = true,
            type = AudioType.REMINDER,
            durationFormatted = "0:08"
        )
    )

    fun getAudioList(context: Context): List<CustomAudioItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val customList = mutableListOf<CustomAudioItem>()
        val jsonString = prefs.getString(KEY_AUDIO_LIST, null)

        if (!jsonString.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val filePath = obj.optString("filePath")
                    // Only include if file actually exists on device
                    if (filePath.isNotEmpty() && File(filePath).exists()) {
                        customList.add(
                            CustomAudioItem(
                                id = obj.getString("id"),
                                title = obj.getString("title"),
                                description = obj.optString("description", "ملف صوتي مخصص"),
                                filePath = filePath,
                                isBuiltIn = false,
                                type = try { AudioType.valueOf(obj.optString("type", "ADHAN")) } catch (e: Exception) { AudioType.ADHAN },
                                durationFormatted = obj.optString("durationFormatted", "ملف مرفوع"),
                                dateAddedMillis = obj.optLong("dateAddedMillis", System.currentTimeMillis())
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioStorageManager", "Error parsing custom audios", e)
            }
        }

        return BUILT_IN_AUDIOS + customList
    }

    fun saveCustomAudio(
        context: Context,
        uri: Uri,
        customTitle: String,
        type: AudioType
    ): CustomAudioItem? {
        return try {
            val audioDir = File(context.filesDir, "custom_audio").apply { if (!exists()) mkdirs() }
            val originalName = getFileName(context, uri) ?: "audio_${System.currentTimeMillis()}.mp3"
            val safeExtension = originalName.substringAfterLast(".", "mp3")
            val targetFile = File(audioDir, "custom_${System.currentTimeMillis()}.$safeExtension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            val finalTitle = if (customTitle.isNotBlank()) customTitle else originalName.substringBeforeLast(".")
            val newItem = CustomAudioItem(
                id = "custom_${System.currentTimeMillis()}",
                title = finalTitle,
                description = "ملف مخصص مرفوع: $originalName",
                filePath = targetFile.absolutePath,
                isBuiltIn = false,
                type = type,
                durationFormatted = "ملف صوتي",
                dateAddedMillis = System.currentTimeMillis()
            )

            // Save to list
            val currentList = getAudioList(context).filter { !it.isBuiltIn }.toMutableList()
            currentList.add(newItem)
            saveCustomList(context, currentList)

            // Auto-select if appropriate
            if (type == AudioType.ADHAN) {
                setSelectedAdhanId(context, newItem.id)
            } else if (type == AudioType.DUA) {
                setSelectedDuaId(context, newItem.id)
            }

            newItem
        } catch (e: Exception) {
            Log.e("AudioStorageManager", "Failed to save audio file", e)
            null
        }
    }

    fun deleteCustomAudio(context: Context, audioId: String): Boolean {
        try {
            val currentList = getAudioList(context).filter { !it.isBuiltIn }.toMutableList()
            val itemToDelete = currentList.find { it.id == audioId }
            if (itemToDelete != null) {
                itemToDelete.filePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) file.delete()
                }
                currentList.removeAll { it.id == audioId }
                saveCustomList(context, currentList)

                // If deleted item was active, reset to default
                if (getSelectedAdhanId(context) == audioId) {
                    setSelectedAdhanId(context, "builtin_default_adhan")
                }
                if (getSelectedDuaId(context) == audioId) {
                    setSelectedDuaId(context, "builtin_dua_after_adhan")
                }
                return true
            }
        } catch (e: Exception) {
            Log.e("AudioStorageManager", "Error deleting audio", e)
        }
        return false
    }

    private fun saveCustomList(context: Context, list: List<CustomAudioItem>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("description", item.description)
                put("filePath", item.filePath)
                put("type", item.type.name)
                put("durationFormatted", item.durationFormatted)
                put("dateAddedMillis", item.dateAddedMillis)
            }
            jsonArray.put(obj)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_AUDIO_LIST, jsonArray.toString()).apply()
    }

    fun getSelectedAdhanId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SELECTED_ADHAN, "builtin_default_adhan") ?: "builtin_default_adhan"
    }

    fun setSelectedAdhanId(context: Context, id: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_ADHAN, id).apply()
    }

    fun getSelectedDuaId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SELECTED_DUA, "builtin_dua_after_adhan") ?: "builtin_dua_after_adhan"
    }

    fun setSelectedDuaId(context: Context, id: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_DUA, id).apply()
    }

    fun isPrayerNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PRAYER_NOTIFICATIONS_ENABLED, true)
    }

    fun setPrayerNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PRAYER_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isPrePrayerAlertEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PRE_PRAYER_ALERT_ENABLED, true)
    }

    fun setPrePrayerAlertEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_PRE_PRAYER_ALERT_ENABLED, enabled).apply()
    }

    fun playAudio(context: Context, audioItem: CustomAudioItem, onCompletion: () -> Unit = {}) {
        stopAudio()
        try {
            val player: MediaPlayer = if (audioItem.rawResId != null) {
                MediaPlayer.create(context, audioItem.rawResId).apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                }
            } else {
                val p = MediaPlayer()
                p.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                if (audioItem.filePath != null && File(audioItem.filePath).exists()) {
                    p.setDataSource(audioItem.filePath)
                    p.prepare()
                } else {
                    val defaultRes = R.raw.adhan_default
                    val uri = Uri.parse("android.resource://" + context.packageName + "/" + defaultRes)
                    p.setDataSource(context, uri)
                    p.prepare()
                }
                p
            }

            player.start()
            _currentlyPlayingId.value = audioItem.id

            mediaPlayer = player
            player.setOnCompletionListener {
                _currentlyPlayingId.value = null
                stopAudio()
                onCompletion()
            }
            player.setOnErrorListener { _, _, _ ->
                _currentlyPlayingId.value = null
                stopAudio()
                true
            }
        } catch (e: Exception) {
            Log.e("AudioStorageManager", "Error playing audio", e)
            _currentlyPlayingId.value = null
            stopAudio()
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            _currentlyPlayingId.value = null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        name = it.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path?.let { p ->
                val cut = p.lastIndexOf('/')
                if (cut != -1) p.substring(cut + 1) else p
            }
        }
        return name
    }
}
