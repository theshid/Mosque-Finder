package com.shid.mosquefinder.Data.Repository

import androidx.lifecycle.LiveData
import com.shid.mosquefinder.Data.database.QuranDao
import com.shid.mosquefinder.Data.database.entities.Surah

class SurahRepository(private val quranDao: QuranDao) {

    fun getAllSurahs(): List<Surah> {
        return quranDao.getSurahs()
    }

    fun getSurahByNumber(surahNumber: Int): Surah = quranDao.getSurahByNumber(surahNumber)

    fun getListSurahs(surahNumber: Int): List<Surah> = quranDao.getSurahList(surahNumber)
}