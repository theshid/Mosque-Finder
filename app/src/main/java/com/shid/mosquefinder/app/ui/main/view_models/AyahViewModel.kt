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
    private val getSurahByNumberUseCase: GetSurahByNumberUseCase,
    private val updateAyahUseCase: UpdateAyahUseCase,
    private val getSurahInFrenchUseCase: GetSurahInFrenchUseCase,
    private val getSurahsForBaseCalculationUseCase: GetSurahsForBaseCalculationUseCase
) : BaseViewModel() {

    private var getAllAyah: Job? = null
    private var getSurahByNumber: Job? = null
    private var getSurahInFrenchJob: Job? = null
    private var getSurahsForCalculationJob: Job? = null

    val surahsForBaseCalculationViewState: LiveData<SurahsForBaseCalculationViewState>
        get() = _surahsForBaseCalculationViewState

    val surahByNumberViewState: LiveData<SurahByNumberViewState>
        get() = _surahByNumberViewState

    val surahInFrenchViewState: LiveData<FrenchSurahViewState>
        get() = _surahInFrenchViewState

    val ayahViewState: LiveData<AyahViewState>
        get() = _ayahViewState

    private var _ayahViewState = MutableLiveData<AyahViewState>()
    private var _surahByNumberViewState = MutableLiveData<SurahByNumberViewState>()
    private var _surahInFrenchViewState = MutableLiveData<FrenchSurahViewState>()
    private var _surahsForBaseCalculationViewState =
        MutableLiveData<SurahsForBaseCalculationViewState>()

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        val message = ExceptionHandler.parse(exception)
        Timber.d("exception:${exception.message}")
        _surahInFrenchViewState.value =
            _surahInFrenchViewState.value?.copy(isLoading = false, error = Error(message))

        _surahByNumberViewState.value =
            _surahByNumberViewState.value?.copy(error = Error(message))

        _surahsForBaseCalculationViewState.value =
            _surahsForBaseCalculationViewState.value?.copy(error = Error(message))

        _ayahViewState.value =
            _ayahViewState.value?.copy(isLoading = false, error = Error(message))
    }

    init {
        _surahInFrenchViewState.value =
            FrenchSurahViewState(isLoading = true, error = null, surahInFrench = null)
        _surahByNumberViewState.value = SurahByNumberViewState(error = null, surah = null)
        _ayahViewState.value = AyahViewState(isLoading = true, error = null, ayahs = null)
        _surahsForBaseCalculationViewState.value =
            SurahsForBaseCalculationViewState(error = null, surahs = null)
    }

    override fun onCleared() {
        super.onCleared()
        getAllAyah?.cancel()
        getSurahByNumber?.cancel()
        getSurahInFrenchJob?.cancel()
        getSurahsForCalculationJob?.cancel()
    }

    fun getAyahs(surahNumber: Int) {
        getAllAyah = launchCoroutine {
            onAyahsLoading()
            loadAyahs(surahNumber)
        }
    }

    fun getSurahsForCalculation(surahNumber: Int) {
        getSurahsForCalculationJob = launchCoroutine {
            loadSurahsForBaseCalculation(surahNumber)
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

    private suspend fun loadSurahsForBaseCalculation(surahNumber: Int) {
        getSurahsForBaseCalculationUseCase(surahNumber).collect { surahsList ->
            onSurahsForCalculationLoadingComplete(surahsList.map { it.toPresentation() })
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

    private fun onSurahsForCalculationLoadingComplete(surahsList: List<SurahPresentation>) {
        _surahsForBaseCalculationViewState.value =
            _surahsForBaseCalculationViewState.value?.copy(surahs = surahsList)
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