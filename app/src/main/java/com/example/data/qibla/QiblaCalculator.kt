package com.example.data.qibla

import kotlin.math.*

object QiblaCalculator {
    const val MAKKAH_LAT = 21.4225
    const val MAKKAH_LNG = 39.8262

    /**
     * Calculates the Qibla bearing in degrees from True North (0..360)
     */
    fun calculateQiblaBearing(lat: Double, lng: Double): Double {
        val latRad = Math.toRadians(lat)
        val lngRad = Math.toRadians(lng)
        val makkahLatRad = Math.toRadians(MAKKAH_LAT)
        val makkahLngRad = Math.toRadians(MAKKAH_LNG)

        val deltaLng = makkahLngRad - lngRad

        val y = sin(deltaLng)
        val x = cos(latRad) * tan(makkahLatRad) - sin(latRad) * cos(deltaLng)

        var qibla = Math.toDegrees(atan2(y, x))
        qibla = (qibla + 360.0) % 360.0
        return qibla
    }

    /**
     * Calculates distance to Makkah in kilometers
     */
    fun calculateDistanceToMakkahKm(lat: Double, lng: Double): Double {
        val r = 6371.0 // Earth radius in km
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
}
