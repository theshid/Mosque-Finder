package com.shid.mosquefinder.Utils

import android.content.Context
import android.content.SharedPreferences
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.R

const val defaultLat = 33.97159194946289F
const val defaultLng = -6.849812984466553F
class SharePref(context: Context) {
    private val mySharePref: SharedPreferences
    val mContext = context

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

    fun setIsFirstTime(firstTime:Boolean){
        val editor = mySharePref.edit()
        editor.putBoolean("first_time",firstTime)
        editor.apply()
    }

    fun setFirstTimeAyah(firstTime:Boolean){
        val editor = mySharePref.edit()
        editor.putBoolean("first_time_ayah",firstTime)
        editor.apply()
    }

    fun setFirstTime(firstTime:Boolean){
        val editor = mySharePref.edit()
        editor.putBoolean("first_time_bis",firstTime)
        editor.apply()
    }

    fun saveSwitchState(state:Boolean){
        val editor = mySharePref.edit()
        editor.putBoolean(mContext.getString(R.string.pref_notification_key),state)
        editor.apply()
    }

    fun saveUser(user:String){
        val editor = mySharePref.edit()
        editor.putString("user",user)
        editor.apply()
    }

    fun loadSwitchState():Boolean{
        val state = mySharePref.getBoolean(mContext.getString(R.string.pref_notification_key),true)
        return state
    }

    fun loadUseCount():Int{
        return mySharePref.getInt("use_count",0)
    }
    fun loadIfUserRated():Boolean{
        return mySharePref.getBoolean("rate",false)
    }
    fun loadSavedPosition(): LatLng {
    val position:LatLng = LatLng(mySharePref.getFloat("position_lat", defaultLat).toDouble(),
    mySharePref.getFloat("position_lon", defaultLng).toDouble())
        return position
    }

    fun loadIsFirstTimePref(): Boolean {
        return mySharePref.getBoolean("first_time", true)
    }

    fun loadFirstTime():Boolean{
        return mySharePref.getBoolean("first_time_bis", true)
    }

    fun loadFirstTimeAyah():Boolean{
        return mySharePref.getBoolean("first_time_ayah", true)
    }

    fun loadUser():String{
        return mySharePref.getString("user","")!!
    }

    init {
        mySharePref = context.getSharedPreferences("filename", Context.MODE_PRIVATE)
    }
}
