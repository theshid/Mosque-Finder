package com.shid.mosquefinder.Utils

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.shid.mosquefinder.R
import java.lang.Exception

fun getErrorMessage(context: Context, ex: Exception): String {
    return if (ex is NetworkException) {
        ex.message!!
    } else {
        Crashlytics.logException(ex)
        context.getString(R.string.default_error_message)
    }
}