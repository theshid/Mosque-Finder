package com.shid.mosquefinder.app.ui.main.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.shid.mosquefinder.app.ui.main.states.BlogViewState
import com.shid.mosquefinder.app.ui.main.states.Error
import com.shid.mosquefinder.app.utils.helper_class.singleton.ExceptionHandler
import com.shid.mosquefinder.data.model.Article
import com.shid.mosquefinder.domain.usecases.GetArticlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class BlogViewModel @Inject constructor(private val getArticlesUseCase: GetArticlesUseCase) :
    BaseViewModel() {

    private var getArticlesJob: Job? = null

    val blogViewState: LiveData<BlogViewState>
        get() = _blogViewState

    private var _blogViewState = MutableLiveData<BlogViewState>()

    override val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        val message = ExceptionHandler.parse(exception)
        _blogViewState.value =
            _blogViewState.value?.copy(isLoading = false, error = Error(message))
    }

    init {
        _blogViewState.value = BlogViewState(isLoading = true, error = null, articles = null)
    }

    override fun onCleared() {
        super.onCleared()
        getArticlesJob?.cancel()
    }

    fun getArticles() {
        getArticlesJob = launchCoroutine {
            onArticlesLoading()
            loadArticles()
        }
    }

    private fun onArticlesLoading() {
        _blogViewState.value = _blogViewState.value?.copy(isLoading = true)
    }

    private suspend fun loadArticles() {
        onArticleLoadingComplete(getArticlesUseCase.invoke(Unit))
    }

    private fun onArticleLoadingComplete(list: List<Article>) {
        _blogViewState.value = _blogViewState.value?.copy(isLoading = false, articles = list)
    }
}