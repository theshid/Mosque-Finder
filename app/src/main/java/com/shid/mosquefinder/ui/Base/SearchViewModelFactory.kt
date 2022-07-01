package com.shid.mosquefinder.ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.data.model.Api.ApiInterface
import com.shid.mosquefinder.data.repository.MapRepository
import com.shid.mosquefinder.ui.Main.ViewModel.SearchViewModel

class SearchViewModelFactory constructor(apiInterface: ApiInterface, application: Application) : ViewModelProvider.Factory  {

    val mApplication = application
    val mApiInterface = apiInterface


    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(MapRepository(mApiInterface,mApplication),mApplication) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}