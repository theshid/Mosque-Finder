package com.shid.mosquefinder.ui.main.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shid.mosquefinder.data.remote.usecases.GetTranslationUseCase
import com.shid.mosquefinder.ui.main.mappers.toPresentation
import com.shid.mosquefinder.ui.main.states.AzkharViewState
import com.shid.mosquefinder.ui.main.states.Error
import com.shid.mosquefinder.utils.ExceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
internal class AzkharViewModel @Inject constructor(private val getTranslationUseCase: GetTranslationUseCase) :
    BaseViewModel() {

    private var translationJob: Job? = null
    val azkharViewState: LiveData<AzkharViewState>
        get() = _azkharViewState

    private var _azkharViewState = MutableLiveData<AzkharViewState>()

    init {
        _azkharViewState.value = AzkharViewState(
            isComplete = false,
            error = null,
            translation = null
        )
    }

    override fun onCleared() {
        super.onCleared()
        translationJob?.cancel()
    }

    /*fun initView(deeplPresentation: DeeplPresentation) {
        _azkharViewState.value = _azkharViewState.value?.copy(translation = deeplPresentation)
    }*/

    fun getTranslation(input: String, isRetry: Boolean = false) {
        if (isRetry) {
            _azkharViewState.value = _azkharViewState.value?.copy(error = null)
        }

        translationJob = launchCoroutine {
            loadTranslation(input)
            _azkharViewState.value = _azkharViewState.value?.copy(isComplete = true)
        }
    }

    fun displayDeeplError(message: Int) {
        _azkharViewState.value = _azkharViewState.value?.copy(error = Error(message))
    }

    private suspend fun loadTranslation(input: String) {
        getTranslationUseCase(input).collect { deepL ->
            val deeplPresentation = deepL.toPresentation()
            _azkharViewState.value = _azkharViewState.value?.copy(translation = deeplPresentation)

        }
    }

    /*private val repository = azkharRepositoryImpl
    var output = repository.translationResponse
    fun setTranslation(input:String){
        repository.setTranslation(input)
    }*/

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        val message = ExceptionHandler.parse(exception)
        _azkharViewState.value = _azkharViewState.value?.copy(error = Error(message))
    }
}