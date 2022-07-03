package com.shid.mosquefinder.utils

import androidx.annotation.StringRes
import com.shid.mosquefinder.R
import java.net.UnknownHostException

object ExceptionHandler {

    @StringRes
    fun parse(t: Throwable): Int {
        return when (t) {
            is UnknownHostException -> R.string.error_check_internet_connection
            else -> R.string.error_oops_error_occured
        }
    }
}