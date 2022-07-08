package com.shid.mosquefinder

import android.app.Application
import android.content.Context

import com.shid.mosquefinder.app.utils.network.ConnectivityStateHolder.registerConnectivityBroadcaster
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    companion object {
        var context: Context? = null
        lateinit var application: App
    }

    override fun onCreate() {
        super.onCreate()

        context = this;
        application = this
        registerConnectivityBroadcaster()
       /* val formatStrategy:FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(true)
            .methodCount(1)
            .methodOffset(5)
            .tag("")
            .build()

        Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                Logger.log(priority,"-$tag",message,t)
            }
        })
*/
        /*SoLoader.init(this, false)

        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(this)) {
            val client = AndroidFlipperClient.getInstance(this)
            client.addPlugin(InspectorFlipperPlugin(this, DescriptorMapping.withDefaults()))
            client.addPlugin(DatabasesFlipperPlugin(this))
            client.start()
        }*/
    }
}