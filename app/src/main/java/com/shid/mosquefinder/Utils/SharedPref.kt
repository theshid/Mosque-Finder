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

    fun saveUseCount(count:Int){
        val editor:SharedPreferences.Editor = mySharePref.edit()
        editor.putInt("use_count",count)
        editor.apply()
    }

    fun saveIfUserRated(rated: Boolean){
        val editor:SharedPreferences.Editor = mySharePref.edit()
        editor.putBoolean("rate",rated)
        editor.apply()
    }

    fun setHelp(firstTime:Boolean){
        val editor = mySharePref.edit()
        editor.putBoolean("first_time",firstTime)
        editor.apply()
    }

    fun loadUseCount():Int{
        return mySharePref.getInt("use_count",0)
    }
    fun loadIfUserRated():Boolean{
        return mySharePref.getBoolean("rate",false)
    }
    fun loadSavedPosition(): LatLng {
    val position:LatLng = LatLng(mySharePref.getFloat("position_lat",15F).toDouble(),
    mySharePref.getFloat("position_lon",15F).toDouble())
        return position
    }

    fun loadHelpPref(): Boolean {
        return mySharePref.getBoolean("first_time", true)
    }

    init {
        mySharePref = context.getSharedPreferences("filename", Context.MODE_PRIVATE)
    }
}
