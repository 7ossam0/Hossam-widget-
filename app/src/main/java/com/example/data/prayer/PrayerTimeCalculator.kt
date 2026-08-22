package com.example.data.prayer

import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.*

enum class CalculationMethod(val displayName: String, val fajrAngle: Double, val ishaAngle: Double, val ishaIntervalMinutes: Int = 0) {
    UMM_AL_QURA("أم القرى (مكة المكرمة)", 18.5, 0.0, 90),
    EGYPTIAN("الهيئة العامة المصرية للمساحة", 19.5, 17.5),
    MWL("رابطة العالم الإسلامي", 18.0, 17.0),
    ISNA("الجمعية الإسلامية لأمريكا الشمالية (ISNA)", 15.0, 15.0),
    KARACHI("جامعة العلوم الإسلامية بكراتشي", 18.0, 18.0),
    DUBAI("دائرة الشؤون الإسلامية بدبي", 18.2, 18.2),
    KUWAIT("وزارة الأوقاف بالكويت", 18.0, 17.5)
}

enum class AsrJuristicMethod(val displayName: String, val shadowFactor: Double) {
    STANDARD("الجمهور (الشافعي، المالكي، الحنبلي)", 1.0),
    HANAFI("المذهب الحنفي", 2.0)
}

enum class CelestialSkyPhase(
    val phaseNameArabic: String,
    val descriptionArabic: String,
    val isDaytime: Boolean,
    val skyGlowColorHex: String
) {
    DEEP_NIGHT("سكون الليل والتهجد", "سماء مرصعة بالنجوم والقمر المضيء", false, "#1A237E"),
    FAJR_DAWN("فجر صادق وبزوغ النور", "خيوط الفجر الأولى بألوان أرجوانية ووردية", false, "#4A148C"),
    SUNRISE_GLOW("شروق الشمس والضياء", "أشعة ذهبية دافئة تملأ الأفق", true, "#FF6F00"),
    MORNING_DUHA("ضحى النهار والنشاط", "سماء نقية زرقاء وشمس مشرقة", true, "#0288D1"),
    NOON_DHUHR("ظهيرة مباركة وتوسط الشمس", "الشمس في كبد السماء بضياء ساطع", true, "#00ACC1"),
    AFTERNOON_ASR("وقت العصر والسكينة", "أجواء هادئة وأشعة مائلة دافئة", true, "#F57C00"),
    GOLDEN_HOUR("الساعة الذهبية والأصيل", "توهج كهرماني ساحر قبل المغيب", true, "#E65100"),
    SUNSET_MAGHRIB("غروب الشمس والشفق", "تدرجات قرمزية وشفق المغيب", false, "#C2185B"),
    DUSK_TWILIGHT("الغسق وحلول العتمة", "بزوغ أولى النجوم وهدوء المساء", false, "#311B92"),
    NIGHT_ISHA("صلاة العشاء وليل هادئ", "قبة سماوية هادئة مع ضوء القمر الفضي", false, "#0D1B2A")
}

data class CityLocation(
    val cityName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneOffsetHours: Double,
    val defaultMethod: CalculationMethod = CalculationMethod.UMM_AL_QURA
)

data class PrayerTime(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val hour: Int,
    val minute: Int,
    val timeFormatted: String,
    val isPassed: Boolean = false,
    val isNext: Boolean = false,
    val isCurrent: Boolean = false
)

data class DailyPrayerSchedule(
    val dateFormatted: String,
    val hijriFormatted: String,
    val cityName: String,
    val fajr: PrayerTime,
    val sunrise: PrayerTime,
    val dhuhr: PrayerTime,
    val asr: PrayerTime,
    val maghrib: PrayerTime,
    val isha: PrayerTime,
    val nextPrayer: PrayerTime,
    val currentPrayer: PrayerTime?,
    val remainingMillisToNext: Long,
    val remainingFormatted: String,
    val currentIntervalProgress: Float, // 0.0f to 1.0f
    val skyPhase: CelestialSkyPhase,
    val sunAltitudeAngle: Double,
    val qiblaAngle: Double,
    val qiblaDistanceKm: Double
)

object PrayerTimeCalculator {

    val PRESET_CITIES = listOf(
        CityLocation("مكة المكرمة", "المملكة العربية السعودية", 21.4225, 39.8262, 3.0, CalculationMethod.UMM_AL_QURA),
        CityLocation("المدينة المنورة", "المملكة العربية السعودية", 24.5247, 39.5692, 3.0, CalculationMethod.UMM_AL_QURA),
        CityLocation("الرياض", "المملكة العربية السعودية", 24.7136, 46.6753, 3.0, CalculationMethod.UMM_AL_QURA),
        CityLocation("جدة", "المملكة العربية السعودية", 21.5433, 39.1728, 3.0, CalculationMethod.UMM_AL_QURA),
        CityLocation("القاهرة", "مصر", 30.0444, 31.2357, 2.0, CalculationMethod.EGYPTIAN),
        CityLocation("الإسكندرية", "مصر", 31.2001, 29.9187, 2.0, CalculationMethod.EGYPTIAN),
        CityLocation("دبي", "الإمارات العربية المتحدة", 25.2048, 55.2708, 4.0, CalculationMethod.DUBAI),
        CityLocation("أبوظبي", "الإمارات العربية المتحدة", 24.4539, 54.3773, 4.0, CalculationMethod.DUBAI),
        CityLocation("القدس الشريف", "فلسطين", 31.7683, 35.2137, 2.0, CalculationMethod.MWL),
        CityLocation("عمّان", "الأردن", 31.9539, 35.9106, 3.0, CalculationMethod.MWL),
        CityLocation("الكويت", "الكويت", 29.3759, 47.9774, 3.0, CalculationMethod.KUWAIT),
        CityLocation("الدوحة", "قطر", 25.2854, 51.5310, 3.0, CalculationMethod.UMM_AL_QURA),
        CityLocation("المنامة", "البحرين", 26.2285, 50.5860, 3.0, CalculationMethod.UMM_AL_QURA),
        CityLocation("مسقط", "عُمان", 23.5880, 58.3829, 4.0, CalculationMethod.MWL),
        CityLocation("بغداد", "العراق", 33.3152, 44.3661, 3.0, CalculationMethod.MWL),
        CityLocation("بيروت", "لبنان", 33.8938, 35.5018, 2.0, CalculationMethod.MWL),
        CityLocation("دمشق", "سوريا", 33.5138, 36.2765, 3.0, CalculationMethod.MWL),
        CityLocation("الرباط", "المغرب", 34.0209, -6.8416, 1.0, CalculationMethod.MWL),
        CityLocation("الدار البيضاء", "المغرب", 33.5731, -7.5898, 1.0, CalculationMethod.MWL),
        CityLocation("الجزائر", "الجزائر", 36.7538, 3.0588, 1.0, CalculationMethod.MWL),
        CityLocation("تونس", "تونس", 36.8065, 10.1815, 1.0, CalculationMethod.MWL),
        CityLocation("طرابلس", "ليبيا", 32.8872, 13.1913, 2.0, CalculationMethod.MWL),
        CityLocation("الخرطوم", "السودان", 15.5007, 32.5599, 2.0, CalculationMethod.EGYPTIAN),
        CityLocation("إسطنبول", "تركيا", 41.0082, 28.9784, 3.0, CalculationMethod.MWL),
        CityLocation("لندن", "المملكة المتحدة", 51.5074, -0.1278, 0.0, CalculationMethod.MWL),
        CityLocation("باريس", "فرنسا", 48.8566, 2.3522, 1.0, CalculationMethod.MWL),
        CityLocation("نيويورك", "الولايات المتحدة", 40.7128, -74.0060, -5.0, CalculationMethod.ISNA)
    )

    fun calculateDailySchedule(
        calendar: Calendar = Calendar.getInstance(),
        location: CityLocation = PRESET_CITIES[0],
        method: CalculationMethod = location.defaultMethod,
        asrMethod: AsrJuristicMethod = AsrJuristicMethod.STANDARD
    ): DailyPrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian Date calculation
        val jd = julianDate(year, month, day) - location.longitude / (15.0 * 24.0)

        // Sun astronomical position
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(radToDeg(atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l))))) / 15.0
        val declination = radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))
        val eqOfTime = q / 15.0 - fixHour(ra)

        // Midday (Dhuhr)
        val dhuhrBase = fixHour(12.0 + location.timeZoneOffsetHours - location.longitude / 15.0 - eqOfTime)
        val dhuhrHours = dhuhrBase

        // Sunrise & Sunset angle is 0.8333 degrees below horizon
        val sunriseDiff = sunAngleTime(0.8333, declination, location.latitude)
        val sunriseHours = dhuhrBase - sunriseDiff
        val sunsetHours = dhuhrBase + sunriseDiff

        // Fajr
        val fajrDiff = sunAngleTime(method.fajrAngle, declination, location.latitude)
        val fajrHours = dhuhrBase - fajrDiff

        // Asr
        val asrDiff = asrAngleTime(asrMethod.shadowFactor, declination, location.latitude)
        val asrHours = dhuhrBase + asrDiff

        // Maghrib
        val maghribHours = sunsetHours + (3.0 / 60.0) // 3 minutes buffer

        // Isha
        val ishaHours = if (method.ishaIntervalMinutes > 0) {
            maghribHours + (method.ishaIntervalMinutes / 60.0)
        } else {
            val ishaDiff = sunAngleTime(method.ishaAngle, declination, location.latitude)
            dhuhrBase + ishaDiff
        }

        // Format to PrayerTime objects
        val fajrTime = createPrayerTime("fajr", "الفجر", "Fajr", fajrHours, calendar)
        val sunriseTime = createPrayerTime("sunrise", "الشروق", "Sunrise", sunriseHours, calendar)
        val dhuhrTime = createPrayerTime("dhuhr", "الظهر", "Dhuhr", dhuhrHours, calendar)
        val asrTime = createPrayerTime("asr", "العصر", "Asr", asrHours, calendar)
        val maghribTime = createPrayerTime("maghrib", "المغرب", "Maghrib", maghribHours, calendar)
        val ishaTime = createPrayerTime("isha", "العشاء", "Isha", ishaHours, calendar)

        val prayersList = listOf(fajrTime, sunriseTime, dhuhrTime, asrTime, maghribTime, ishaTime)

        // Determine current time
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)
        val currentDayMinutes = currentHour * 60 + currentMinute + currentSecond / 60.0

        // Find current and next prayer
        var nextPrayer = fajrTime
        var currentPrayer: PrayerTime? = null
        var foundNext = false

        for (i in prayersList.indices) {
            val prayer = prayersList[i]
            val prayerMinutes = prayer.hour * 60 + prayer.minute
            if (currentDayMinutes < prayerMinutes) {
                nextPrayer = prayer
                currentPrayer = if (i > 0) prayersList[i - 1] else prayersList.last()
                foundNext = true
                break
            }
        }

        if (!foundNext) {
            // Next is tomorrow's Fajr
            nextPrayer = fajrTime
            currentPrayer = ishaTime
        }

        // Remaining time calculation
        val nowMillis = calendar.timeInMillis
        val nextCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, nextPrayer.hour)
            set(Calendar.MINUTE, nextPrayer.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= nowMillis) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        val remainingMillis = max(0L, nextCal.timeInMillis - nowMillis)
        val remHours = (remainingMillis / (1000 * 60 * 60)).toInt()
        val remMinutes = ((remainingMillis % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        val remSeconds = ((remainingMillis % (1000 * 60)) / 1000).toInt()
        val remainingFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", remHours, remMinutes, remSeconds)

        // Calculate progression in interval
        val curCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            val cur = currentPrayer ?: ishaTime
            set(Calendar.HOUR_OF_DAY, cur.hour)
            set(Calendar.MINUTE, cur.minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis > nowMillis) {
                add(Calendar.DAY_OF_MONTH, -1)
            }
        }
        val intervalTotal = max(1L, nextCal.timeInMillis - curCal.timeInMillis)
        val intervalElapsed = max(0L, nowMillis - curCal.timeInMillis)
        val progress = (intervalElapsed.toFloat() / intervalTotal.toFloat()).coerceIn(0.0f, 1.0f)

        // Celestial Sky Phase
        val skyPhase = determineCelestialPhase(currentDayMinutes, fajrHours * 60, sunriseHours * 60, dhuhrHours * 60, asrHours * 60, maghribHours * 60, ishaHours * 60)

        // Sun altitude angle approximate
        val sunAltitude = sin(degToRad(location.latitude)) * sin(degToRad(declination)) +
                cos(degToRad(location.latitude)) * cos(degToRad(declination)) * cos(degToRad((currentDayMinutes / 4.0) - 180.0))

        // Qibla calculation towards Makkah (21.4225° N, 39.8262° E)
        val (qiblaBearing, distanceKm) = calculateQibla(location.latitude, location.longitude)

        // Hijri date estimation
        val hijriStr = estimateHijriDate(calendar)

        val updatedPrayers = prayersList.map { p ->
            val pMinutes = p.hour * 60 + p.minute
            p.copy(
                isPassed = currentDayMinutes > pMinutes,
                isNext = p.id == nextPrayer.id,
                isCurrent = p.id == currentPrayer?.id
            )
        }

        return DailyPrayerSchedule(
            dateFormatted = String.format(Locale.getDefault(), "%d/%02d/%02d", year, month, day),
            hijriFormatted = hijriStr,
            cityName = location.cityName,
            fajr = updatedPrayers[0],
            sunrise = updatedPrayers[1],
            dhuhr = updatedPrayers[2],
            asr = updatedPrayers[3],
            maghrib = updatedPrayers[4],
            isha = updatedPrayers[5],
            nextPrayer = nextPrayer,
            currentPrayer = currentPrayer,
            remainingMillisToNext = remainingMillis,
            remainingFormatted = remainingFormatted,
            currentIntervalProgress = progress,
            skyPhase = skyPhase,
            sunAltitudeAngle = radToDeg(asin(sunAltitude.coerceIn(-1.0, 1.0))),
            qiblaAngle = qiblaBearing,
            qiblaDistanceKm = distanceKm
        )
    }

    private fun determineCelestialPhase(
        nowMin: Double,
        fajrMin: Double,
        sunriseMin: Double,
        dhuhrMin: Double,
        asrMin: Double,
        maghribMin: Double,
        ishaMin: Double
    ): CelestialSkyPhase {
        return when {
            nowMin < (fajrMin - 45) -> CelestialSkyPhase.DEEP_NIGHT
            nowMin < sunriseMin -> CelestialSkyPhase.FAJR_DAWN
            nowMin < (sunriseMin + 60) -> CelestialSkyPhase.SUNRISE_GLOW
            nowMin < (dhuhrMin - 30) -> CelestialSkyPhase.MORNING_DUHA
            nowMin < asrMin -> CelestialSkyPhase.NOON_DHUHR
            nowMin < (maghribMin - 60) -> CelestialSkyPhase.AFTERNOON_ASR
            nowMin < maghribMin -> CelestialSkyPhase.GOLDEN_HOUR
            nowMin < (maghribMin + 45) -> CelestialSkyPhase.SUNSET_MAGHRIB
            nowMin < ishaMin -> CelestialSkyPhase.DUSK_TWILIGHT
            nowMin < 1400 -> CelestialSkyPhase.NIGHT_ISHA
            else -> CelestialSkyPhase.DEEP_NIGHT
        }
    }

    private fun createPrayerTime(id: String, nameAr: String, nameEn: String, rawHours: Double, cal: Calendar): PrayerTime {
        val fixed = fixHour(rawHours)
        val h = fixed.toInt()
        val m = ((fixed - h) * 60 + 0.5).toInt()
        val adjustedH = if (m >= 60) (h + 1) % 24 else h
        val adjustedM = m % 60
        val isPm = adjustedH >= 12
        val h12 = if (adjustedH % 12 == 0) 12 else adjustedH % 12
        val amPmAr = if (isPm) "م" else "ص"
        val formatted = String.format(Locale.getDefault(), "%02d:%02d %s", h12, adjustedM, amPmAr)
        return PrayerTime(
            id = id,
            nameArabic = nameAr,
            nameEnglish = nameEn,
            hour = adjustedH,
            minute = adjustedM,
            timeFormatted = formatted
        )
    }

    private fun calculateQibla(lat: Double, lng: Double): Pair<Double, Double> {
        val makkahLat = 21.4225
        val makkahLng = 39.8262

        val phi1 = degToRad(lat)
        val phi2 = degToRad(makkahLat)
        val deltaLambda = degToRad(makkahLng - lng)

        val y = sin(deltaLambda)
        val x = cos(phi1) * tan(phi2) - sin(phi1) * cos(deltaLambda)
        var qibla = radToDeg(atan2(y, x))
        qibla = (qibla + 360.0) % 360.0

        // Distance formula (Haversine)
        val r = 6371.0 // Earth radius in km
        val dLat = phi2 - phi1
        val dLon = deltaLambda
        val a = sin(dLat / 2).pow(2) + cos(phi1) * cos(phi2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = r * c

        return Pair(qibla, distance)
    }

    private fun estimateHijriDate(cal: Calendar): String {
        return try {
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val jd = julianDate(y, m, d).toLong()
            val l = jd - 1948440L + 10632L
            val n = ((l - 1L) / 10631L)
            val l2 = l - 10631L * n + 354L
            val j = ((10985L - l2) / 5316L) * ((50L * l2) / 17719L) + (l2 / 5670L) * ((43L * l2) / 15238L)
            val l3 = l2 - ((30L - j) / 15L) * ((17719L * j) / 50L) - (j / 16L) * ((15238L * j) / 43L) + 29L
            val hijriMonth = ((24L * l3) / 709L).toInt()
            val hijriDay = (l3 - ((709L * hijriMonth) / 24L)).toInt().coerceIn(1, 30)
            val hijriYear = ((30L * n) + j - 30L).toInt()

            val hijriMonthNames = listOf(
                "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
                "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
            )
            val monthIdx = (hijriMonth - 1).coerceIn(0, 11)
            "$hijriDay ${hijriMonthNames[monthIdx]} $hijriYear هـ"
        } catch (e: Throwable) {
            "١٤٤٨ هـ"
        }
    }

    // Mathematical helper functions
    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunAngleTime(angle: Double, declination: Double, latitude: Double): Double {
        val cosH = (-sin(degToRad(angle)) - sin(degToRad(latitude)) * sin(degToRad(declination))) /
                (cos(degToRad(latitude)) * cos(degToRad(declination)))
        if (cosH > 1.0 || cosH < -1.0) return 0.0
        return radToDeg(acos(cosH)) / 15.0
    }

    private fun asrAngleTime(factor: Double, declination: Double, latitude: Double): Double {
        val angle = -radToDeg(atan(1.0 / (factor + tan(degToRad(abs(latitude - declination))))))
        return sunAngleTime(angle, declination, latitude)
    }

    private fun degToRad(deg: Double): Double = deg * PI / 180.0
    private fun radToDeg(rad: Double): Double = rad * 180.0 / PI
    private fun fixAngle(a: Double): Double = (a % 360.0 + 360.0) % 360.0
    private fun fixHour(h: Double): Double = (h % 24.0 + 24.0) % 24.0
}
