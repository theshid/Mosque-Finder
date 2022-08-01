package com.shid.mosquefinder.domain.repository

import com.shid.mosquefinder.domain.model.Surah
import kotlinx.coroutines.flow.Flow

interface SurahRepository {

    fun getAllSurahs(): List<Surah>

    fun getSurahByNumber(surahNumber: Int): Surah

    fun getListSurahs(surahNumber: Int): List<Surah>
}