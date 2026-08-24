package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.dao.QuranCacheDao
import com.example.data.model.QuranSurahCacheEntity
import com.example.data.quran.Ayah
import com.example.data.quran.QuranApiService
import com.example.data.quran.QuranDataProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class QuranRepository(
    private val context: Context,
    private val quranCacheDao: QuranCacheDao
) {
    private val TAG = "QuranRepository"
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val ayahListType = Types.newParameterizedType(List::class.java, Ayah::class.java)
    private val ayahListAdapter = moshi.adapter<List<Ayah>>(ayahListType)

    // In-memory cache for ultra-fast instant UI rendering
    private val memoryCache = mutableMapOf<Int, List<Ayah>>()

    /**
     * Returns a Flow that emits immediately the local/embedded Ayahs,
     * checks Room database cache, and fetches full authentic online verses in background
     * without ever blocking the UI.
     */
    fun getAyahsForSurahFlow(surahNumber: Int, surahName: String): Flow<List<Ayah>> = flow {
        // 1. Check memory cache first
        val mem = memoryCache[surahNumber]
        if (mem != null && mem.isNotEmpty()) {
            emit(mem)
            return@flow
        }

        // 2. Check Room Database cache
        val dbCache = quranCacheDao.getSurahCache(surahNumber)
        if (dbCache != null && dbCache.ayahsJson.isNotBlank()) {
            try {
                val parsed = ayahListAdapter.fromJson(dbCache.ayahsJson)
                if (parsed != null && parsed.isNotEmpty()) {
                    memoryCache[surahNumber] = parsed
                    emit(parsed)
                    return@flow
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing cached ayahs: ${e.message}")
            }
        }

        // 3. Emit offline verified/built-in catalog (never capped at 10)
        val localAyahs = QuranDataProvider.getAyahsForSurah(surahNumber)
        emit(localAyahs)

        // 4. If not complete in local map, fetch full authentic Uthmani text online
        try {
            val onlineAyahs = QuranApiService.fetchSurahAyahsOnline(surahNumber, surahName)
            if (onlineAyahs != null && onlineAyahs.isNotEmpty()) {
                memoryCache[surahNumber] = onlineAyahs
                val json = ayahListAdapter.toJson(onlineAyahs)
                quranCacheDao.insertSurahCache(QuranSurahCacheEntity(surahNumber, json))
                emit(onlineAyahs)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Online fetch skipped or failed: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getSurahAyahs(surahNumber: Int, surahName: String): List<Ayah> = withContext(Dispatchers.IO) {
        val mem = memoryCache[surahNumber]
        if (mem != null && mem.isNotEmpty()) return@withContext mem

        val dbCache = quranCacheDao.getSurahCache(surahNumber)
        if (dbCache != null && dbCache.ayahsJson.isNotBlank()) {
            try {
                val parsed = ayahListAdapter.fromJson(dbCache.ayahsJson)
                if (parsed != null && parsed.isNotEmpty()) {
                    memoryCache[surahNumber] = parsed
                    return@withContext parsed
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing cached ayahs: ${e.message}")
            }
        }

        val online = QuranApiService.fetchSurahAyahsOnline(surahNumber, surahName)
        if (online != null && online.isNotEmpty()) {
            memoryCache[surahNumber] = online
            try {
                val json = ayahListAdapter.toJson(online)
                quranCacheDao.insertSurahCache(QuranSurahCacheEntity(surahNumber, json))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext online
        }

        return@withContext QuranDataProvider.getAyahsForSurah(surahNumber)
    }
}
