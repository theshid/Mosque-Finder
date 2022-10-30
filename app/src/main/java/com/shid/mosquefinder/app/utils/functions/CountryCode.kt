package com.shid.mosquefinder.app.utils

import android.content.Context
import android.telephony.TelephonyManager

fun getCountryCode(context: Context): String {
    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return tm.networkCountryIso
}