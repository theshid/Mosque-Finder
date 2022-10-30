package com.shid.mosquefinder.app.utils.helper_class

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.google.android.gms.maps.model.LatLng
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_ACTIVATE
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_FILENAME
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_FIRST_TIME
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_FIRST_TIME_AYAH
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_FIRST_TIME_BIS
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_FIRST_TIME_PRAYER
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_POSITION_LAT
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_POSITION_LONG
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_RATE
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_REMINDER
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_TOKEN
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_USER
import com.shid.mosquefinder.app.utils.helper_class.Constants.PREF_USE_COUNT
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common

const val defaultLat = 33.971592F
const val defaultLng = -6.849813F


class SharePref(context: Context) {
    private val mySharePref: SharedPreferences =
        context.getSharedPreferences(PREF_FILENAME, Context.MODE_PRIVATE)
    private val mContext = context
    var editor: SharedPreferences.Editor = mySharePref.edit()


    fun saveUserPosition(savedPosition: LatLng) {
        editor.putFloat(PREF_POSITION_LAT, savedPosition.latitude.toFloat())
        editor.putFloat(PREF_POSITION_LONG, savedPosition.longitude.toFloat())
        editor.apply()
    }

    fun saveUseCount(count: Int) {
        editor.putInt(PREF_USE_COUNT, count)
        editor.apply()
    }

    fun saveIfUserRated(rated: Boolean) {
        editor.putBoolean(PREF_RATE, rated)
        editor.apply()
    }

    fun setIsFirstTime(firstTime: Boolean) {
        editor.putBoolean(PREF_FIRST_TIME, firstTime)
        editor.apply()
    }

    fun setFirstTimeAyah(firstTime: Boolean) {
        editor.putBoolean(PREF_FIRST_TIME_AYAH, firstTime)
        editor.apply()
    }

    fun setFirstTimePrayerNotification(firstTime: Boolean) {
        editor.putBoolean(PREF_FIRST_TIME_PRAYER, firstTime)
        editor.apply()
    }

    fun setAllPrayerNotifications(activateAll: Boolean) {
        editor.putBoolean(PREF_ACTIVATE, activateAll)
        editor.apply()
    }


    fun setFirstTime(firstTime: Boolean) {
        editor.putBoolean(PREF_FIRST_TIME_BIS, firstTime)
        editor.apply()
    }

    fun saveSwitchState(state: Boolean) {
        editor.putBoolean(mContext.getString(R.string.pref_notification_key), state)
        editor.apply()
    }

    fun saveUser(user: String) {
        editor.putString(PREF_USER, user)
        editor.apply()
    }

    fun saveFirebaseToken(token: String) {
        if (TextUtils.isEmpty(token)) {
            return
        }
        editor.putString(PREF_TOKEN, token)
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

    fun saveFajrId(id: Int) {
        editor.putInt(Common.FAJR_ID, id)
        editor.apply()
    }

    fun saveDhurId(id: Int) {
        editor.putInt(Common.DHUR_ID, id)
        editor.apply()
    }

    fun saveAsrId(id: Int) {
        editor.putInt(Common.ASR_ID, id)
        editor.apply()
    }

    fun saveMaghribId(id: Int) {
        editor.putInt(Common.MAGHRIB_ID, id)
        editor.apply()
    }

    fun saveIshaId(id: Int) {
        editor.putInt(Common.ISHA_ID, id)
        editor.apply()
    }

    fun loadFajrId(): Int {
        return mySharePref.getInt(Common.FAJR_ID, 0)
    }

    fun loadDhurId(): Int {
        return mySharePref.getInt(Common.DHUR_ID, 0)
    }

    fun loadAsrId(): Int {
        return mySharePref.getInt(Common.ASR_ID, 0)
    }

    fun loadMaghribId(): Int {
        return mySharePref.getInt(Common.MAGHRIB_ID, 0)
    }

    fun loadIshaId(): Int {
        return mySharePref.getInt(Common.ISHA_ID, 0)
    }

    fun loadIsAllPrayersNotificationActivated(): Boolean {
        return mySharePref.getBoolean(PREF_ACTIVATE, false)
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
        return mySharePref.getString(PREF_REMINDER, "").toString()
    }

    fun loadFirebaseToken(): String {
        return mySharePref.getString(PREF_TOKEN, "").toString()
    }

    fun loadSwitchState(): Boolean {
        val state = mySharePref.getBoolean(mContext.getString(R.string.pref_notification_key), true)
        return state
    }

    fun loadUseCount(): Int {
        return mySharePref.getInt(PREF_USE_COUNT, 0)
    }

    fun loadIfUserRated(): Boolean {
        return mySharePref.getBoolean(PREF_RATE, false)
    }

    fun loadSavedPosition(): LatLng {
        return LatLng(
            mySharePref.getFloat(PREF_POSITION_LAT, defaultLat).toDouble(),
            mySharePref.getFloat(PREF_POSITION_LONG, defaultLng).toDouble()
        )
    }

    fun loadIsFirstTimePref(): Boolean {
        return mySharePref.getBoolean(PREF_FIRST_TIME, true)
    }

    fun loadFirstTime(): Boolean {
        return mySharePref.getBoolean(PREF_FIRST_TIME_BIS, true)
    }

    fun loadFirstTimeAyah(): Boolean {
        return mySharePref.getBoolean(PREF_FIRST_TIME_AYAH, true)
    }

    fun loadFirstTimePrayerNotification(): Boolean {
        return mySharePref.getBoolean(PREF_FIRST_TIME_PRAYER, true)
    }

    fun loadUser(): String {
        return mySharePref.getString(PREF_USER, "")!!
    }

    fun isAppInBackground(state: Boolean) {
        editor.apply {
            putBoolean(Constants.PREF_APP_STATE_KEY, state)
            apply()
        }
    }

    fun getIsAppInBackground() = mySharePref.getBoolean(Constants.PREF_APP_STATE_KEY, false)


}
