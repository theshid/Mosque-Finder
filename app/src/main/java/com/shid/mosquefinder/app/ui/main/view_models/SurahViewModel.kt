package com.shid.mosquefinder.app.ui.main.view_models

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.shid.mosquefinder.app.ui.main.mappers.toPresentation
import com.shid.mosquefinder.app.ui.main.states.Error
import com.shid.mosquefinder.app.ui.main.states.SurahViewState
import com.shid.mosquefinder.app.ui.models.SurahPresentation
import com.shid.mosquefinder.app.utils.doAsync
import com.shid.mosquefinder.app.utils.enums.Prayers
import com.shid.mosquefinder.app.utils.helper_class.singleton.ExceptionHandler
import com.shid.mosquefinder.app.utils.helper_class.singleton.TimeUtil.day
import com.shid.mosquefinder.app.utils.helper_class.singleton.TimeUtil.hour
import com.shid.mosquefinder.domain.usecases.GetAllSurahsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
internal class SurahViewModel @Inject constructor(private val getAllSurahsUseCase: GetAllSurahsUseCase) :
    BaseViewModel() {

    private var getAllSurahsJob: Job? = null

    val surahViewState: LiveData<SurahViewState>
        get() = _surahViewState

    private var _surahViewState = MutableLiveData<SurahViewState>()

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Timber.d("Exception:${exception.message}")
        val message = ExceptionHandler.parse(exception)
        _surahViewState.value =
            _surahViewState.value?.copy(isLoading = false, error = Error(message))
    }

    private var _list = MutableLiveData<List<SurahPresentation>>()
    val surahDbList: LiveData<List<SurahPresentation>>
        get() = _list

    //private var repository: SurahRepositoryImpl
    var nextPray = MutableStateFlow("-")
    var descNextPray = MutableStateFlow("-")
    private lateinit var countDownTimer: CountDownTimer


    init {
        _surahViewState.value = SurahViewState(isLoading = true, error = null, surahs = null)
    }

    override fun onCleared() {
        super.onCleared()
        getAllSurahsJob?.cancel()
    }

    fun getSurahs() {

        getAllSurahsJob = launchCoroutine {
            Timber.d("inside Coroutine")
            onSurahsLoading()
            loadSurahs()
        }
        /*viewModelScope.launch(Dispatchers.IO) {
            onSurahsLoading()
            loadSurahs()
            *//*val listSurah = repository.getAllSurahs()
            _list.postValue(listSurah)*//*
        }*/
    }

    fun test() {
        viewModelScope.launch(Dispatchers.IO) {
            //onSurahsLoading()
            loadSurahs()
            /*val listSurah = repository.getAllSurahs()
            _list.postValue(listSurah)*/
        }
    }

    private suspend fun loadSurahs() {
        getAllSurahsUseCase.invoke(Unit).collect { surahList ->
            val surahsList = surahList.map { it.toPresentation() }
            Timber.d("surahs: $surahsList ")
            //_list.postValue(surahsList)
            onSurahsLoadingComplete(surahsList)
        }
        /*getAllSurahsUseCase(Unit).collect{surahs ->
            val surahsList = surahs.map { it.toPresentation() }
            Timber.d("surahs: $surahsList ")
            onSurahsLoadingComplete(surahsList)
        }*/
    }

    private fun onSurahsLoadingComplete(surahsList: List<SurahPresentation>) {
        _surahViewState.value = _surahViewState.value?.copy(isLoading = false, surahs = surahsList)
    }

    private fun onSurahsLoading() {
        _surahViewState.value = _surahViewState.value?.copy(isLoading = true)
    }

    fun update() {
        doAsync {
            // val context = application.applicationContext
            //val pushId = runBlocking { getPushId(context) }
        }
    }

    fun getIntervalText(prayerName: Prayers, prayerTime: String) = viewModelScope.launch {
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


    /*private suspend fun getPushId(context: Context):String?{
        val sharePref = SharePref(context)
        val pushId = sharePref.loadFirebaseToken()
        return if (pushId.isNullOrBlank()){
            Common.retrievePushId(context)
        }else{
            pushId
        }
    }*/


}