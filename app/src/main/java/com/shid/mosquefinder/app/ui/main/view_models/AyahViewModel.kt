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
import com.shid.mosquefinder.app.utils.helper_class.singleton.Common
import com.shid.mosquefinder.app.utils.helper_class.singleton.ExceptionHandler
import com.shid.mosquefinder.data.local.database.entities.AyahDb
import com.shid.mosquefinder.data.local.database.entities.SurahDb
import com.shid.mosquefinder.data.model.pojo.Root
import com.shid.mosquefinder.data.model.pojo.Verse
import com.shid.mosquefinder.domain.usecases.GetAllSurahsUseCase
import com.shid.mosquefinder.domain.usecases.GetAyahUseCase
import com.shid.mosquefinder.domain.usecases.GetSurahByNumberUseCase
import com.shid.mosquefinder.domain.usecases.UpdateAyahUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class AyahViewModel @Inject constructor(
    private val getAyahUsecase: GetAyahUseCase,
    private val getAllSurahsUseCase: GetAllSurahsUseCase,
    private val getSurahByNumberUseCase: GetSurahByNumberUseCase,
    private val updateAyahUseCase: UpdateAyahUseCase
) : BaseViewModel() {

    private var getAllAyah: Job? = null
    private var getAllSurahs: Job? = null
    private var getSurahByNumber: Job? = null

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
        _ayahViewState.value = AyahViewState(isLoading = true, error = null, surahs = null)
    }

    override fun onCleared() {
        super.onCleared()
        getAllAyah?.cancel()
        getAllSurahs?.cancel()
    }

    private val repository = ayahRepositoryImpl
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

    var _translation = MutableLiveData<List<Verse>>()
    val translation: LiveData<List<Verse>>
        get() = _translation

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
        _ayahViewState.value = _ayahViewState.value?.copy(isLoading = false, surahs = ayahsList)
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

    fun getFrenchSurah(surahId: Int) {
        service.getFrenchSurah(surahId).enqueue(object : Callback<Root> {
            override fun onResponse(call: Call<Root>, response: Response<Root>) {
                if (response.code() == 200) {
                    _translation.value = response.body()!!.data.verse
                    /* GlobalScope.launch(Dispatchers.IO){
                         quranDao.updateAyah(response.body()!!.data.verse,ayahId)
                     }*/

                    Log.d("Ayah", "OnResponse OK : " + response.body()!!.data.verse + " " + surahId)
                } else {

                    Log.d("Ayah", "OnResponse Fail : ")
                }
            }

            override fun onFailure(call: Call<Root>, t: Throwable) {

                Log.d("Ayah", "OnResponse Fail : ")
            }

        })
    }
}