package com.shid.mosquefinder.Ui.Base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shid.mosquefinder.Data.Model.Api.ApiInterface
import com.shid.mosquefinder.Data.Repository.MapRepository
import com.shid.mosquefinder.Ui.Main.ViewModel.AuthViewModel
import com.shid.mosquefinder.Ui.Main.ViewModel.SearchViewModel

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