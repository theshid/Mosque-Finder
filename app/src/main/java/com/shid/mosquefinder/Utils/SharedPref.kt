package com.shid.mosquefinder.Utils

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng

class SharePref(context: Context) {
    private val mySharePref: SharedPreferences

    fun saveUserPosition(savedPosition: LatLng) {
        val editor = mySharePref.edit()
        editor.putFloat("position_lat", savedPosition.latitude.toFloat())
        editor.putFloat("position_lon", savedPosition.longitude.toFloat())
        editor.apply()
    }

    fun loadSavedPosition(): LatLng {
    val position:LatLng = LatLng(mySharePref.getFloat("position_lat",15F).toDouble(),
    mySharePref.getFloat("position_lon",15F).toDouble())
        return position
    }

    fun loadNightMode(): Boolean {
        return mySharePref.getBoolean("NightMode", false)
    }

    init {
        mySharePref = context.getSharedPreferences("filename", Context.MODE_PRIVATE)
    }
}
