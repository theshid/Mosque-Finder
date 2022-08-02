package com.shid.mosquefinder.app.di

import com.shid.mosquefinder.domain.repository.AyahRepository
import com.shid.mosquefinder.domain.repository.AzkharRepository
import com.shid.mosquefinder.domain.repository.SurahRepository
import com.shid.mosquefinder.domain.usecases.*
import dagger.Provides
import javax.inject.Singleton

object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetAllSurahsUseCase(surahRepository: SurahRepository): GetAllSurahsUseCase =
        GetAllSurahsUseCase(surahRepository)

    @Provides
    @Singleton
    fun provideGetTranslationUseCase(azkharRepository: AzkharRepository): GetTranslationUseCase =
        GetTranslationUseCase(azkharRepository)

    @Provides
    @Singleton
    fun provideGetAllAyahUseCase(ayahRepository: AyahRepository): GetAyahUseCase {
        return GetAyahUseCase(ayahRepository)
    }

    @Provides
    @Singleton
    fun provideGetSurahInFrenchUseCase(ayahRepository: AyahRepository): GetSurahInFrenchUseCase {
        return GetSurahInFrenchUseCase(ayahRepository)
    }

    @Provides
    @Singleton
    fun provideGetSurahByNumberUseCase(surahRepository: SurahRepository): GetSurahByNumberUseCase {
        return GetSurahByNumberUseCase(surahRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateAyahUseCase(ayahRepository: AyahRepository):UpdateAyahUseCase{
        return UpdateAyahUseCase(ayahRepository)
    }
}