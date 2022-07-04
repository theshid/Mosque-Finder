package com.shid.mosquefinder.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.R

const val defaultLat = 33.97159194946289F
const val defaultLng = -6.849812984466553F

class SharePref(context: Context) {
    private val mySharePref: SharedPreferences =
        context.getSharedPreferences("filename", Context.MODE_PRIVATE)
    val mContext = context
    var editor: SharedPreferences.Editor = mySharePref.edit()


    fun saveUserPosition(savedPosition: LatLng) {
        editor.putFloat("position_lat", savedPosition.latitude.toFloat())
        editor.putFloat("position_lon", savedPosition.longitude.toFloat())
        editor.apply()
    }

    fun saveUseCount(count: Int) {
        editor.putInt("use_count", count)
        editor.apply()
    }

    fun saveIfUserRated(rated: Boolean) {
        editor.putBoolean("rate", rated)
        editor.apply()
    }

    fun setIsFirstTime(firstTime: Boolean) {
        editor.putBoolean("first_time", firstTime)
        editor.apply()
    }

    fun setFirstTimeAyah(firstTime: Boolean) {
        editor.putBoolean("first_time_ayah", firstTime)
        editor.apply()
    }

    fun setFirstTimePrayerNotification(firstTime: Boolean) {
        editor.putBoolean("first_time_prayer", firstTime)
        editor.apply()
    }

    fun setAllPrayerNotifications(activateAll: Boolean) {
        editor.putBoolean("activate", activateAll)
        editor.apply()
    }


    fun setFirstTime(firstTime: Boolean) {
        editor.putBoolean("first_time_bis", firstTime)
        editor.apply()
    }

    fun saveSwitchState(state: Boolean) {
        editor.putBoolean(mContext.getString(R.string.pref_notification_key), state)
        editor.apply()
    }

    fun saveUser(user: String) {
        editor.putString("user", user)
        editor.apply()
    }

    fun saveFirebaseToken(token: String) {
        if (TextUtils.isEmpty(token)) {
            return
        }
        editor.putString("token", token)
        editor.apply()
    }

    fun saveFajr(prayer: String) {
        editor.putString(Common.FAJR, prayer)
        editor.apply()
    }

    fun saveDhur(prayer: String) {
        editor.putString(Common.DHUR, prayer)
        editor.apply()
    }

    fun saveAsr(prayer: String) {
        editor.putString(Common.ASR, prayer)
        editor.apply()
    }

    fun saveMaghrib(prayer: String) {
        editor.putString(Common.MAGHRIB, prayer)
        editor.apply()
    }

    fun saveIsha(prayer: String) {
        editor.putString(Common.ISHA, prayer)
        editor.apply()
    }

    fun saveFajrState(state: Boolean) {
        editor.putBoolean(Common.FAJR_STATE, state)
        editor.apply()
    }

    fun saveDhurState(state: Boolean) {
        editor.putBoolean(Common.DHUR_STATE, state)
        editor.apply()
    }

    fun saveAsrState(state: Boolean) {
        editor.putBoolean(Common.ASR_STATE, state)
        editor.apply()
    }

    fun saveMaghribState(state: Boolean) {
        editor.putBoolean(Common.MAGHRIB_STATE, state)
        editor.apply()
    }

    fun saveIshaState(state: Boolean) {
        editor.putBoolean(Common.ISHA_STATE, state)
        editor.apply()
    }

    fun loadIsAllPrayersNotificationActivated(): Boolean {
        return mySharePref.getBoolean("activate", false)
    }

    fun loadFajrState(): Boolean {
        return mySharePref.getBoolean(Common.FAJR_STATE, false)
    }

    fun loadDhurState(): Boolean {
        return mySharePref.getBoolean(Common.DHUR_STATE, false)
    }

    fun loadAsrState(): Boolean {
        return mySharePref.getBoolean(Common.ASR_STATE, false)
    }

    fun loadMaghribState(): Boolean {
        return mySharePref.getBoolean(Common.MAGHRIB_STATE, false)
    }

    fun loadIshaState(): Boolean {
        return mySharePref.getBoolean(Common.ISHA_STATE, false)
    }

    fun loadFajr(): String {
        return mySharePref.getString(Common.FAJR, "").toString()
    }

    fun loadDhur(): String {
        return mySharePref.getString(Common.DHUR, "").toString()
    }

    fun loadAsr(): String {
        return mySharePref.getString(Common.ASR, "").toString()
    }

    fun loadMaghrib(): String {
        return mySharePref.getString(Common.MAGHRIB, "").toString()
    }

    fun loadIsha(): String {
        return mySharePref.getString(Common.ISHA, "").toString()
    }

    fun isReminderSet(): String {
        return mySharePref.getString("reminder", "").toString()
    }

    fun loadFirebaseToken(): String {
        return mySharePref.getString("token", "").toString()
    }

    fun loadSwitchState(): Boolean {
        val state = mySharePref.getBoolean(mContext.getString(R.string.pref_notification_key), true)
        return state
    }

    fun loadUseCount(): Int {
        return mySharePref.getInt("use_count", 0)
    }

    fun loadIfUserRated(): Boolean {
        return mySharePref.getBoolean("rate", false)
    }

    fun loadSavedPosition(): LatLng {
        return LatLng(
            mySharePref.getFloat("position_lat", defaultLat).toDouble(),
            mySharePref.getFloat("position_lon", defaultLng).toDouble()
        )
    }

    fun loadIsFirstTimePref(): Boolean {
        return mySharePref.getBoolean("first_time", true)
    }

    fun loadFirstTime(): Boolean {
        return mySharePref.getBoolean("first_time_bis", true)
    }

    fun loadFirstTimeAyah(): Boolean {
        return mySharePref.getBoolean("first_time_ayah", true)
    }

    fun loadFirstTimePrayerNotification(): Boolean {
        return mySharePref.getBoolean("first_time_prayer", true)
    }

    fun loadUser(): String {
        return mySharePref.getString("user", "")!!
    }


}
