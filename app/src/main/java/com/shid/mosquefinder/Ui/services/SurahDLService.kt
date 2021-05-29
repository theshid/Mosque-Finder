package com.shid.mosquefinder.Ui.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.shid.mosquefinder.Ui.services.downloadUtil.NotificationHelper
import com.shid.mosquefinder.Ui.services.downloadUtil.NotificationHelper.Companion.NOTIFICATION_ID
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import zlc.season.downloadx.core.DownloadParam
import zlc.season.downloadx.download

class SurahDLService : Service() {
    private val helper by lazy { NotificationHelper(this) }
    // Binder given to clients
    private val binder = LocalBinder()
    /*private val path = "/storage/emulated/0/Android/data/com.shid.mosquefinder/files/surahs/"*/
    private val path = this.getExternalFilesDir(null).toString() + "/surahs"
    private var surahNumber:Int ?= null


    override fun onBind(intent: Intent): IBinder ?{
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val lien = intent?.getStringExtra("link")
        surahNumber = intent?.getIntExtra("number",1)
        val surahName = intent?.getStringExtra("surah")
        val fileName = "$surahNumber-$surahName.mp3"
        if (lien != null){
            downloadMedia(lien,fileName)
        }

        return START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()

    }

    private fun downloadMedia(url:String,nameFile:String){
        if (surahNumber!= null){
            helper.surahNumber = surahNumber as Int
        }

        val notification = helper.getNotification()
        startForeground(NOTIFICATION_ID,notification)
        val downloadParam = DownloadParam(url, nameFile, path)
        val downloadTask = GlobalScope.download(downloadParam)
        downloadTask.progress()
            .onEach { progress ->  helper.notificationBuilder.setProgress(100,progress.percent().toInt(),false)
                helper.updateNotification("In Progress")
                if (progress.percent().toInt() == 100){
                    helper.notificationBuilder.setProgress(0,0,false)
                        .setOngoing(false)
                        .setAutoCancel(true)
                    helper.updateNotification("Download Completed!")
                    stopService()
                }}
            .launchIn(GlobalScope) // using lifecycleScope

        downloadTask.state()
            .onEach { state ->
                // update state

                Log.d("Test","state"+state)
                // update progress
                //setProgress(state.progress)
            }
            .launchIn(GlobalScope)
        downloadTask.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }

    private fun stopService(){
        stopForeground(true)
        stopSelf()
    }

    //removing service when user swipe out our app
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    inner class LocalBinder: Binder() {
        fun getDLService():SurahDLService = this@SurahDLService
    }


}