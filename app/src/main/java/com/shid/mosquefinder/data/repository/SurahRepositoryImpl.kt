package com.shid.mosquefinder.data.repository

import com.shid.mosquefinder.data.local.database.QuranDao
import com.shid.mosquefinder.data.local.database.entities.SurahDb
import com.shid.mosquefinder.data.local.toDomain
import com.shid.mosquefinder.domain.model.Surah
import com.shid.mosquefinder.domain.repository.SurahRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SurahRepositoryImpl @Inject constructor(private val quranDao: QuranDao) : SurahRepository {

    override fun getAllSurahs(): Flow<List<Surah>> = flow {
        val surahs =  quranDao.getSurahs()
        emit(surahs.map { surahDb -> surahDb.toDomain() })
    }

    override fun getSurahByNumber(surahNumber: Int): Flow<SurahDb> = quranDao.getSurahByNumber(surahNumber)

    override fun getListSurahs(surahNumber: Int): Flow<List<SurahDb>> = quranDao.getSurahList(surahNumber)
}