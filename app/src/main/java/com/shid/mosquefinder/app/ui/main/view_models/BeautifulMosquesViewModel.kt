package com.shid.mosquefinder.app.ui.main.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shid.mosquefinder.app.ui.main.states.BeautyMosqueViewState
import com.shid.mosquefinder.app.ui.main.states.Error
import com.shid.mosquefinder.app.utils.helper_class.singleton.ExceptionHandler
import com.shid.mosquefinder.data.model.BeautifulMosques
import com.shid.mosquefinder.domain.usecases.GetBeautifulMosquesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class BeautifulMosquesViewModel @Inject constructor(var getBeautifulMosquesUseCase: GetBeautifulMosquesUseCase) :
    BaseViewModel() {

    private var getBeautyMosquesJob: Job? = null

    val beautyMosqueViewState: LiveData<BeautyMosqueViewState>
        get() = _beautyMosqueViewState

    private var _beautyMosqueViewState = MutableLiveData<BeautyMosqueViewState>()

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        val message = ExceptionHandler.parse(exception)
        _beautyMosqueViewState.value =
            _beautyMosqueViewState.value?.copy(isLoading = false, error = Error(message))
    }

    init {
        _beautyMosqueViewState.value =
            BeautyMosqueViewState(isLoading = true, error = null, mosques = null)
    }

    override fun onCleared() {
        super.onCleared()
        getBeautyMosquesJob?.cancel()
    }

    fun getBeautifulMosques() {
        getBeautyMosquesJob = launchCoroutine {
            onBeautifulMosquesLoading()
            loadBeautifulMosques()
        }
    }

    private fun onBeautifulMosquesLoading() {
        _beautyMosqueViewState.value = _beautyMosqueViewState.value?.copy(isLoading = true)
    }

    private suspend fun loadBeautifulMosques() {
        onBeautifulMosquesLoadingComplete(getBeautifulMosquesUseCase.invoke(Unit))
    }

    private fun onBeautifulMosquesLoadingComplete(list: List<BeautifulMosques>) {
        _beautyMosqueViewState.value =
            _beautyMosqueViewState.value?.copy(isLoading = false, mosques = list)
    }

}