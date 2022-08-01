package com.shid.mosquefinder.data.repository

import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.local.toDomain
import com.shid.mosquefinder.domain.model.Surah
import com.shid.mosquefinder.domain.repository.SurahRepository
import javax.inject.Inject

class SurahRepositoryImpl @Inject constructor(private val quranDao: QuranDao) : SurahRepository {

    override fun getAllSurahs(): List<Surah> {
        val surahs = quranDao.getSurahs()
        return surahs.map { surahDb ->
            surahDb.toDomain()
        }
    }

    override fun getSurahByNumber(surahNumber: Int): Surah {
        return quranDao.getSurahByNumber(surahNumber).toDomain()
    }

    override fun getListSurahs(surahNumber: Int): List<Surah> {
        return quranDao.getSurahList(surahNumber).map { surahDb -> surahDb.toDomain() }
    }
}