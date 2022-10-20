package com.shid.mosquefinder.app.ui.main.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shid.mosquefinder.app.ui.main.states.Error
import com.shid.mosquefinder.app.ui.main.states.QuoteViewState
import com.shid.mosquefinder.app.utils.helper_class.singleton.ExceptionHandler
import com.shid.mosquefinder.data.model.Quotes
import com.shid.mosquefinder.domain.usecases.GetQuotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class QuotesViewModel @Inject constructor(private val getQuotesUseCase: GetQuotesUseCase) :
    BaseViewModel() {

    private var getQuotesJob: Job? = null

    val quoteViewState: LiveData<QuoteViewState>
        get() = _quoteViewState

    private var _quoteViewState = MutableLiveData<QuoteViewState>()

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        val message = ExceptionHandler.parse(exception)
        _quoteViewState.value =
            _quoteViewState.value?.copy(isLoading = false, error = Error(message))
    }

    init {
        _quoteViewState.value = QuoteViewState(isLoading = true, error = null, quotes = null)
    }

    override fun onCleared() {
        super.onCleared()
        getQuotesJob?.cancel()
    }

    fun getQuotes() {
        getQuotesJob = launchCoroutine {
            onQuotesLoading()
            loadQuotes()
        }
    }

    private fun onQuotesLoading() {
        _quoteViewState.value = _quoteViewState.value?.copy(isLoading = true)
    }

    private suspend fun loadQuotes() {
        onQuotesLoadingComplete(getQuotesUseCase.invoke(Unit))
    }

    private fun onQuotesLoadingComplete(list: List<Quotes>) {
        _quoteViewState.value = _quoteViewState.value?.copy(isLoading = false, quotes = list)
    }

}