package com.example.data.quran

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val nameTranslation: String,
    val versesCount: Int,
    val type: SurahType,
    val pageNumber: Int,
    val juzNumber: Int
)

enum class SurahType(val labelArabic: String) {
    MAKKI("مكية"),
    MADANI("مدنية")
}

data class Ayah(
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val textUthmani: String,
    val textSimple: String,
    val tafsirMuyassar: String,
    val asbabNuzul: String? = null,
    val pageNumber: Int = 1,
    val juzNumber: Int = 1
)

data class JuzInfo(
    val juzNumber: Int,
    val startSurahName: String,
    val startAyah: Int,
    val pageNumber: Int
)
