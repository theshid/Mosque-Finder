package com.shid.mosquefinder.ui.Main.ViewModel

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.shid.mosquefinder.data.repository.SurahRepository
import com.shid.mosquefinder.data.database.QuranDatabase
import com.shid.mosquefinder.data.database.entities.Surah
import com.shid.mosquefinder.utils.Common
import com.shid.mosquefinder.utils.SharePref
import com.shid.mosquefinder.utils.TimeUtil.day
import com.shid.mosquefinder.utils.TimeUtil.hour
import com.shid.mosquefinder.utils.doAsync
import com.shid.mosquefinder.utils.enums.Prayers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class SurahViewModel(private val application: Application) : ViewModel() {
    private var _list = MutableLiveData<List<Surah>>()
    val surahList: LiveData<List<Surah>>
        get() = _list
    private var repository: SurahRepository
    var nextPray = MutableStateFlow("-")
    var descNextPray = MutableStateFlow("-")
    private lateinit var countDownTimer: CountDownTimer


    init {
        val quranDao = QuranDatabase.getDatabase(application, viewModelScope, application.resources)
            .surahDao()
        repository = SurahRepository(quranDao)
        /*viewModelScope.launch(Dispatchers.IO) {
            listSurahs = repository.getAllSurahs()
        }*/
        //listSurahs = repository.getAllSurahs()
    }
    fun getSurahs(){
        viewModelScope.launch(Dispatchers.IO){
            val listSurah = repository.getAllSurahs()
            _list.postValue(listSurah)
        }
    }

    fun update(){
        doAsync {
            val context = application.applicationContext
            val pushId = runBlocking { getPushId(context) }
        }
    }

    fun getIntervalText(prayerName: Prayers, prayerTime:String) = viewModelScope.launch {
        val now = Timestamp.now()
        val diff: Long = Date(
            Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, if (now.hour > 20) now.day + 1 else now.day)
                set(Calendar.HOUR_OF_DAY, prayerTime.substringBefore(":").toInt())
                set(Calendar.MINUTE, prayerTime.substringAfter(":").toInt())
            }.time.time
        ).time - now.toDate().time
        if (this@SurahViewModel::countDownTimer.isInitialized) countDownTimer.cancel()
        countDownTimer = object : CountDownTimer(diff, 1000) {
            override fun onTick(progress: Long) {
                val seconds = progress / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                nextPray.value = "${hours}h ${minutes - (hours * 60)}m ${seconds - (minutes * 60)}s"
                descNextPray.value = buildString {
                    append(" to ")
                    append(prayerName.prayer)
                }
            }

            override fun onFinish() {
                if (this@SurahViewModel::countDownTimer.isInitialized) countDownTimer.cancel()
                nextPray.value = "Now"
                descNextPray.value = buildString {
                    append(" it's time to pray ")
                    append(prayerName.prayer)
                }
            }
        }.start()
    }


    private suspend fun getPushId(context: Context):String?{
        val sharePref = SharePref(context)
        val pushId = sharePref.loadFirebaseToken()
        return if (pushId.isNullOrBlank()){
            Common.retrievePushId(context)
        }else{
            pushId
        }
    }

  
}