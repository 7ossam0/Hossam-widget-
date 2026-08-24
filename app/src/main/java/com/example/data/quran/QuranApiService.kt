package com.example.data.quran

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object QuranApiService {
    private const val TAG = "QuranApiService"

    /**
     * Fetches complete authentic Uthmani Quran verses and Tafsir Muyassar for a given surah
     * from the official open Quran Cloud API.
     */
    suspend fun fetchSurahAyahsOnline(surahNumber: Int, surahNameArabic: String): List<Ayah>? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val urlString = "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,ar.muyassar"
            val url = URL(urlString)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 10000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "BayanQuranApp/2.1")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseText = reader.use { it.readText() }
                return@withContext parseQuranApiResponse(responseText, surahNumber, surahNameArabic)
            } else {
                Log.w(TAG, "Quran API returned code: ${connection.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch surah $surahNumber online: ${e.message}")
            null
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Parses the JSON response from api.alquran.cloud with 2 editions:
     * 1. quran-uthmani (Uthmani text)
     * 2. ar.muyassar (Tafsir Al-Muyassar)
     */
    private fun parseQuranApiResponse(
        jsonString: String,
        surahNumber: Int,
        surahNameArabic: String
    ): List<Ayah> {
        val root = JSONObject(jsonString)
        val dataArray = root.getJSONArray("data")

        if (dataArray.length() < 1) return emptyList()

        val uthmaniEdition = dataArray.getJSONObject(0)
        val uthmaniAyahs = uthmaniEdition.getJSONArray("ayahs")

        // Check if Tafsir edition exists
        val tafsirMap = mutableMapOf<Int, String>()
        if (dataArray.length() >= 2) {
            val tafsirEdition = dataArray.getJSONObject(1)
            val tafsirAyahs = tafsirEdition.getJSONArray("ayahs")
            for (i in 0 until tafsirAyahs.length()) {
                val item = tafsirAyahs.getJSONObject(i)
                val ayahNum = item.getInt("numberInSurah")
                val text = item.getString("text")
                tafsirMap[ayahNum] = text
            }
        }

        val result = mutableListOf<Ayah>()

        for (i in 0 until uthmaniAyahs.length()) {
            val item = uthmaniAyahs.getJSONObject(i)
            val ayahNum = item.getInt("numberInSurah")
            var textUthmani = item.getString("text")

            // Remove Bismillah from first ayah if not Al-Fatihah (standard Uthmani API quirk)
            if (surahNumber != 1 && ayahNum == 1 && textUthmani.startsWith("بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ")) {
                textUthmani = textUthmani.removePrefix("بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ").trim()
            }

            val simpleText = textUthmani
                .replace(Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]"), "") // Strip harakat for search
                .replace("ٱ", "ا")

            val pageNumber = item.optInt("page", 1)
            val juzNumber = item.optInt("juz", 1)
            val tafsir = tafsirMap[ayahNum] ?: "تفسير ميسر للآية الكريمة $ayahNum من سورة $surahNameArabic."

            result.add(
                Ayah(
                    surahNumber = surahNumber,
                    surahName = surahNameArabic,
                    ayahNumber = ayahNum,
                    textUthmani = textUthmani,
                    textSimple = simpleText,
                    tafsirMuyassar = tafsir,
                    asbabNuzul = if (ayahNum == 1) "سورة $surahNameArabic مكية/مدنية عدد آياتها ${uthmaniAyahs.length()} آية." else null,
                    pageNumber = pageNumber,
                    juzNumber = juzNumber
                )
            )
        }

        return result
    }
}
