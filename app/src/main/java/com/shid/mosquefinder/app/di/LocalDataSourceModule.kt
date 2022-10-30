package com.shid.mosquefinder.app.di

import com.shid.mosquefinder.data.local.database.daos.QuranDao
import com.shid.mosquefinder.data.repository.SurahRepositoryImpl
import com.shid.mosquefinder.domain.repository.SurahRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDataSourceModule {

    @Provides
    @Singleton
    fun provideSurahRepository(dao: QuranDao): SurahRepository = SurahRepositoryImpl(dao)
}