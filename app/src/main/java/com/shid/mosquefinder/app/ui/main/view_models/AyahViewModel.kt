package com.shid.mosquefinder.app.ui.main.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shid.mosquefinder.app.ui.main.mappers.toPresentation
import com.shid.mosquefinder.app.ui.main.states.AyahViewState
import com.shid.mosquefinder.app.ui.main.states.Error
import com.shid.mosquefinder.app.ui.main.states.SurahViewState
import com.shid.mosquefinder.app.ui.models.AyahPresentation
import com.shid.mosquefinder.app.ui.models.SurahPresentation
import com.shid.mosquefinder.app.ui.models.VersePresentation
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common
import com.shid.mosquefinder.app.utils.helper_class.singleton.ExceptionHandler
import com.shid.mosquefinder.data.local.database.entities.AyahDb
import com.shid.mosquefinder.data.local.database.entities.SurahDb
import com.shid.mosquefinder.data.model.pojo.RootResponse
import com.shid.mosquefinder.data.model.pojo.VerseResponse
import com.shid.mosquefinder.domain.usecases.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
 class AyahViewModel @Inject constructor(
    private val getAyahUsecase: GetAyahUseCase,
    private val getAllSurahsUseCase: GetAllSurahsUseCase,
    private val getSurahByNumberUseCase: GetSurahByNumberUseCase,
    private val updateAyahUseCase: UpdateAyahUseCase,
    private val getSurahInFrenchUseCase: GetSurahInFrenchUseCase
) : BaseViewModel() {

    private var getAllAyah: Job? = null
    private var getAllSurahs: Job? = null
    private var getSurahByNumber: Job? = null
    private var getSurahInFrenchJob: Job? = null

    val surahsViewState: LiveData<SurahViewState>
        get() = _surahsViewState

    val ayahViewState: LiveData<AyahViewState>
        get() = _ayahViewState

    private var _ayahViewState = MutableLiveData<AyahViewState>()
    private var _surahsViewState = MutableLiveData<SurahViewState>()

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        val message = ExceptionHandler.parse(exception)
        _surahsViewState.value =
            _surahsViewState.value?.copy(isLoading = false, error = Error(message))
        _ayahViewState.value =
            _ayahViewState.value?.copy(isLoading = false, error = Error(message))
    }

    init {
        _surahsViewState.value = SurahViewState(isLoading = true, error = null, surahs = null)
        _ayahViewState.value = AyahViewState(isLoading = true, error = null, ayahs = null)
    }

    override fun onCleared() {
        super.onCleared()
        getAllAyah?.cancel()
        getAllSurahs?.cancel()
    }


    val service = Common.frenchQuranApiService

    private var _ayah = MutableLiveData<List<AyahDb>>()
    val ayah: LiveData<List<AyahDb>>
        get() = _ayah

    private var _surah = MutableLiveData<SurahPresentation>()
    val surah: LiveData<SurahPresentation>
        get() = _surah

    private var _listSurahDb = MutableLiveData<List<SurahDb>>()
    val listSurahDb: LiveData<List<SurahDb>>
        get() = _listSurahDb

    var _translation = MutableLiveData<List<VerseResponse>>()
    val translation: LiveData<List<VerseResponse>>
        get() = _translation

    var _traduction = MutableLiveData<List<VersePresentation>>()
    val traduction: LiveData<List<VersePresentation>>
        get() = _traduction

    fun getAyahs(surahNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            onAyahsLoading()
            loadAyahs(surahNumber)
        }
    }

    fun getSurahs() {
        viewModelScope.launch(Dispatchers.IO) {
            onSurahsLoading()
            loadSurahs()
        }
    }

    private suspend fun loadAyahs(surahNumber: Int) {
        val ayahsList = getAyahUsecase(surahNumber).map { it.toPresentation() }
        Timber.d("surahs: $ayahsList ")
        onAyahsLoadingComplete(ayahsList)
    }


    private fun onAyahsLoading() {
        _ayahViewState.value = _ayahViewState.value?.copy(isLoading = true)
    }

    private fun onAyahsLoadingComplete(ayahsList: List<AyahPresentation>) {
        _ayahViewState.value = _ayahViewState.value?.copy(isLoading = false, ayahs = ayahsList)
    }

    private suspend fun loadSurahs() {
        val surahsList = getAllSurahsUseCase(Unit).map { it.toPresentation() }
        Timber.d("surahs: $surahsList ")
        onSurahsLoadingComplete(surahsList)
    }


    private fun onSurahsLoading() {
        _surahsViewState.value = _surahsViewState.value?.copy(isLoading = true)
    }

    private fun onSurahsLoadingComplete(surahsList: List<SurahPresentation>) {
        _surahsViewState.value =
            _surahsViewState.value?.copy(isLoading = false, surahs = surahsList)
    }

   /* fun getSurahList(surahNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = surahRepositoryImpl.getListSurahs(surahNumber)
            _listSurahDb.postValue(list)
        }
    }*/


    fun getSurahInfo(surahNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val surah = getSurahByNumberUseCase(surahNumber).toPresentation()
            _surah.postValue(surah)
        }
    }

    /*fun getAllAyah(surahNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val ayahs = repository.getAyah(surahNumber)
            _ayah.postValue(ayahs)
        }
    }*/

    /*fun fetchFrenchSurah(ayahId:Int){
        repository.getFrenchSurah(ayahId)
    }*/

    fun updateAyah(text: String, id: Long) {
        val pairData = Pair(text,id)
        viewModelScope.launch(Dispatchers.IO) {
            updateAyahUseCase(pairData)
        }

    }

    private suspend fun loadSurahInFrench(surahId: Int){
        getSurahInFrenchUseCase(surahId).collect{
            ayahInFrench -> _traduction.value= ayahInFrench.map { it.toPresentation() }
        }
    }

    fun getFrenchSurah(surahId: Int) {
        getSurahInFrenchJob = launchCoroutine {

        }
        service.getFrenchSurah(surahId).enqueue(object : Callback<RootResponse> {
            override fun onResponse(call: Call<RootResponse>, response: Response<RootResponse>) {
                if (response.code() == 200) {
                    _translation.value = response.body()!!.data.verseResponse

                    Log.d("Ayah", "OnResponse OK : " + response.body()!!.data.verseResponse + " " + surahId)
                } else {

                    Log.d("Ayah", "OnResponse Fail : ")
                }
            }

            override fun onFailure(call: Call<RootResponse>, t: Throwable) {

                Log.d("Ayah", "OnResponse Fail : ")
            }

        })
    }
}