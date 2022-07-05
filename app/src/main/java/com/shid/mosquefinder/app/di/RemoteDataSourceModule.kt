package com.shid.mosquefinder.app.di

import android.content.res.Resources
import com.shid.mosquefinder.data.api.DeeplApiInterface
import com.shid.mosquefinder.data.repository.AzkharRepositoryImpl
import com.shid.mosquefinder.domain.repository.AzkharRepository
import dagger.Provides
import javax.inject.Singleton

object RemoteDataSourceModule {

    @Provides
    @Singleton
    fun provideAzkharTranslation(resource: Resources, api: DeeplApiInterface): AzkharRepository =
        AzkharRepositoryImpl(resource, api)
}