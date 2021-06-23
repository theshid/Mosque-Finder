package com.shid.mosquefinder

import android.app.Application
import android.content.Context

import com.shid.mosquefinder.ConnectivityStateHolder.registerConnectivityBroadcaster


class App : Application() {

    companion object {
        var context: Context? = null
        lateinit var application:App
    }

    override fun onCreate() {
        super.onCreate()

        context = this;
        application = this
        registerConnectivityBroadcaster()

       /* SoLoader.init(this, false)

        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            val client = AndroidFlipperClient.getInstance(this)
            client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
            client.addPlugin(DatabasesFlipperPlugin(this))
            client.start()
        }*/
    }
}