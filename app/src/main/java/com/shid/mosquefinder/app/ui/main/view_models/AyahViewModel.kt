package com.shid.mosquefinder.app.ui.main.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shid.mosquefinder.app.ui.main.mappers.toPresentation
import com.shid.mosquefinder.app.ui.main.states.*
import com.shid.mosquefinder.app.ui.models.AyahPresentation
import com.shid.mosquefinder.app.ui.models.SurahPresentation
import com.shid.mosquefinder.app.ui.models.VersePresentation
import com.shid.mosquefinder.app.utils.helper_class.singleton.ExceptionHandler
import com.shid.mosquefinder.domain.usecases.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
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

    val surahByNumberViewState: LiveData<SurahByNumberViewState>
        get() = _surahByNumberViewState

    val surahInFrenchViewState: LiveData<FrenchSurahViewState>
        get() = _surahInFrenchViewState

    val ayahViewState: LiveData<AyahViewState>
        get() = _ayahViewState

    private var _ayahViewState = MutableLiveData<AyahViewState>()
    private var _surahsViewState = MutableLiveData<SurahViewState>()
    private var _surahByNumberViewState = MutableLiveData<SurahByNumberViewState>()
    private var _surahInFrenchViewState = MutableLiveData<FrenchSurahViewState>()

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        val message = ExceptionHandler.parse(exception)
        Timber.d("exception:${exception.message}")
        _surahInFrenchViewState.value =
            _surahInFrenchViewState.value?.copy(isLoading = false, error = Error(message))

        _surahsViewState.value =
            _surahsViewState.value?.copy(isLoading = false, error = Error(message))

        _surahByNumberViewState.value =
            _surahByNumberViewState.value?.copy(error = Error(message))

        _ayahViewState.value =
            _ayahViewState.value?.copy(isLoading = false, error = Error(message))
    }

    init {
        _surahsViewState.value = SurahViewState(isLoading = true, error = null, surahs = null)
        _surahInFrenchViewState.value =
            FrenchSurahViewState(isLoading = true, error = null, surahInFrench = null)
        _surahByNumberViewState.value = SurahByNumberViewState(error = null, surah = null)
        _ayahViewState.value = AyahViewState(isLoading = true, error = null, ayahs = null)
    }

    override fun onCleared() {
        super.onCleared()
        getAllAyah?.cancel()
        getAllSurahs?.cancel()
        getSurahByNumber?.cancel()
        getSurahInFrenchJob?.cancel()
    }

    fun getAyahs(surahNumber: Int) {
        getAllAyah = launchCoroutine {
            onAyahsLoading()
            loadAyahs(surahNumber)
        }
    }

    fun getSurahs() {
        getAllSurahs = launchCoroutine {
            onSurahsLoading()
            loadSurahs()
        }
    }

    fun getFrenchSurahJob(surahId: Int) {
        getSurahInFrenchJob = launchCoroutine {
            onFrenchSurahLoading()
            loadSurahInFrench(surahId)
            delay(2000)
        }
    }

    fun getSurahByNumber(surahNumber: Int) {
        getSurahByNumber = launchCoroutine {
            getSurahInfo(surahNumber)
        }
    }

    private suspend fun loadAyahs(surahNumber: Int) {
        getAyahUsecase(surahNumber).collect { ayahList ->
            val ayahsList = ayahList.map { it.toPresentation() }
            Timber.d("surahs: $ayahsList ")
            onAyahsLoadingComplete(ayahsList)
        }
    }

    private fun onAyahsLoading() {
        _ayahViewState.value = _ayahViewState.value?.copy(isLoading = true)
    }

    private fun onAyahsLoadingComplete(ayahsList: List<AyahPresentation>) {
        _ayahViewState.value = _ayahViewState.value?.copy(isLoading = false, ayahs = ayahsList)
    }

    private suspend fun loadSurahs() {
        getAllSurahsUseCase.invoke(Unit).collect { surahList ->
            val surahs = surahList.map { it.toPresentation() }
            onSurahsLoadingComplete(surahs)
        }
    }

    private fun onSurahsLoading() {
        _surahsViewState.value = _surahsViewState.value?.copy(isLoading = true)
    }

    private fun onSurahsLoadingComplete(surahsList: List<SurahPresentation>) {
        _surahsViewState.value =
            _surahsViewState.value?.copy(isLoading = false, surahs = surahsList)
    }

    private fun onSurahLoadingComplete(surah: SurahPresentation) {
        _surahByNumberViewState.value =
            _surahByNumberViewState.value?.copy(surah = surah)
    }

    private suspend fun getSurahInfo(surahNumber: Int) {
        getSurahByNumberUseCase(surahNumber).collect {
            onSurahLoadingComplete(it.toPresentation())
        }

    }

    fun updateAyah(text: String, id: Long) {
        val pairData = Pair(text, id)

        viewModelScope.launch(Dispatchers.IO) {
            updateAyahUseCase(pairData)
        }

    }

    private fun onFrenchSurahLoading() {
        _surahInFrenchViewState.value = _surahInFrenchViewState.value?.copy(isLoading = true)
    }

    private fun onFrenchSurahLoadingComplete(surahInFrench: List<VersePresentation>) {
        _surahInFrenchViewState.value =
            _surahInFrenchViewState.value?.copy(isLoading = false, surahInFrench = surahInFrench)
    }

    private suspend fun loadSurahInFrench(surahId: Int) {
        getSurahInFrenchUseCase(surahId).collect { ayahInFrench ->
            Timber.d("french list:$ayahInFrench")
            onFrenchSurahLoadingComplete(ayahInFrench.map { it.toPresentation() })
        }
    }

}