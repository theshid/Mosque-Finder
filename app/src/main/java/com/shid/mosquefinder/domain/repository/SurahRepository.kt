package com.shid.mosquefinder.domain.repository

import com.shid.mosquefinder.domain.model.Surah
import kotlinx.coroutines.flow.Flow

interface SurahRepository {

    fun getAllSurahs(): Flow<List<Surah>>

    fun getSurahByNumber(surahNumber: Int): Flow<Surah>

    fun getListSurahsForBaseCalculation(surahNumber: Int): Flow<List<Surah>>
}