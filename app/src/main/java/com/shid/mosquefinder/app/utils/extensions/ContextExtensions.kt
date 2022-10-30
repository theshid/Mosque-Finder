package com.shid.mosquefinder.app.utils.extensions

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.shid.mosquefinder.R

internal inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    block(intent)
    startActivity(intent)
}

internal inline fun <reified T : Service> Context.startService(block: Intent.() -> Unit = {}) {
    val intent = Intent(this, T::class.java)
    block(intent)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        startForegroundService(intent)
        Toast.makeText(this, getString(R.string.dl_started), Toast.LENGTH_LONG).show()
    } else {
        startService(intent)
        Toast.makeText(this, getString(R.string.dl_started), Toast.LENGTH_LONG).show()
    }
}

internal fun Context.showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}

internal fun Context.loadColor(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}