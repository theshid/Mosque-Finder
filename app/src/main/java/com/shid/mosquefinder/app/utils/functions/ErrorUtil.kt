package com.shid.mosquefinder.app.utils.functions

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.shid.mosquefinder.R
import com.shid.mosquefinder.app.utils.helper_class.NetworkException
import java.lang.Exception

fun getErrorMessage(context: Context, ex: Exception): String {
    return if (ex is NetworkException) {
        ex.message!!
    } else {
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.recordException(ex)
        context.getString(R.string.default_error_message)
    }
}