package com.example.data.qibla

import kotlin.math.*

object QiblaCalculator {
    // Kaaba Coordinates (Makkah Al-Mukarramah)
    const val MAKKAH_LAT = 21.422487
    const val MAKKAH_LNG = 39.826206

    /**
     * Calculates the accurate Great-Circle Qibla bearing in degrees from True North (0..360)
     * using the geodesic forward azimuth formula.
     */
    fun calculateQiblaBearing(lat: Double, lng: Double): Double {
        val latRad = Math.toRadians(lat)
        val lngRad = Math.toRadians(lng)
        val makkahLatRad = Math.toRadians(MAKKAH_LAT)
        val makkahLngRad = Math.toRadians(MAKKAH_LNG)

        val deltaLng = makkahLngRad - lngRad

        val y = sin(deltaLng) * cos(makkahLatRad)
        val x = cos(latRad) * sin(makkahLatRad) - sin(latRad) * cos(makkahLatRad) * cos(deltaLng)

        var qibla = Math.toDegrees(atan2(y, x))
        qibla = (qibla + 360.0) % 360.0
        return qibla
    }

    /**
     * Calculates geodesic distance to Makkah in kilometers using the Haversine formula
     */
    fun calculateDistanceToMakkahKm(lat: Double, lng: Double): Double {
        val r = 6371.0 // Earth mean radius in km
        val lat1 = Math.toRadians(lat)
        val lat2 = Math.toRadians(MAKKAH_LAT)
        val dLat = Math.toRadians(MAKKAH_LAT - lat)
        val dLng = Math.toRadians(MAKKAH_LNG - lng)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Returns compass cardinal direction in Arabic (e.g. جنوب شرق، شمال...)
     */
    fun getCompassDirectionArabic(degrees: Double): String {
        val deg = (degrees + 360.0) % 360.0
        return when {
            deg >= 337.5 || deg < 22.5 -> "شمال (N)"
            deg >= 22.5 && deg < 67.5 -> "شمال شرق (NE)"
            deg >= 67.5 && deg < 112.5 -> "شرق (E)"
            deg >= 112.5 && deg < 157.5 -> "جنوب شرق (SE)"
            deg >= 157.5 && deg < 202.5 -> "جنوب (S)"
            deg >= 202.5 && deg < 247.5 -> "جنوب غرب (SW)"
            deg >= 247.5 && deg < 292.5 -> "غرب (W)"
            deg >= 292.5 && deg < 337.5 -> "شمال غرب (NW)"
            else -> "شمال (N)"
        }
    }
}

