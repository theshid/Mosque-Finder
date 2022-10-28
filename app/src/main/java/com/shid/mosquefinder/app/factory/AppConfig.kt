package com.shid.mosquefinder.app.factory

import android.net.Uri
import java.time.format.DateTimeFormatter

data class AppConfig(
    val databaseName:String,
    val databaseCacheSize:Int,
    val workerStateStoreName:String,
    val hostName:Uri,
    val dateFormat:DateTimeFormatter,
)
