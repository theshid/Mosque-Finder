package com.shid.mosquefinder.app.di

import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.repository.SurahRepositoryImpl
import com.shid.mosquefinder.domain.repository.SurahRepository
import dagger.Provides
import javax.inject.Singleton

object LocalDataSourceModule {

    @Provides
    @Singleton
    fun provideSurahRepository(dao: QuranDao): SurahRepository = SurahRepositoryImpl(dao)
}