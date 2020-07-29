package com.shid.mosquefinder

import android.app.Application
import android.content.Context
import com.shid.mosquefinder.ConnectivityStateHolder.registerConnectivityBroadcaster

class App : Application() {

    companion object {
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()

        context = this;
        registerConnectivityBroadcaster()
    }
}