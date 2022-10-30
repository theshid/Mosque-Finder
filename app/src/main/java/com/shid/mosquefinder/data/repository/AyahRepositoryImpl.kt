package com.shid.mosquefinder.data.repository

import com.shid.mosquefinder.data.api.QuranApiInterface
import com.shid.mosquefinder.data.local.database.daos.QuranDao
import com.shid.mosquefinder.data.local.toDomain
import com.shid.mosquefinder.data.remote.toDomain
import com.shid.mosquefinder.domain.model.Ayah
import com.shid.mosquefinder.domain.model.Verse
import com.shid.mosquefinder.domain.repository.AyahRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AyahRepositoryImpl @Inject constructor(
    private val quranDao: QuranDao,
    private val api: QuranApiInterface
) : AyahRepository {

    override fun getAyah(surahNumber: Int): Flow<List<Ayah>> = flow {
        val ayahList = quranDao.getAyah(surahNumber)
        emit(ayahList.map { it.toDomain() })
    }


    override suspend fun updateAyah(text: String, ayahId: Long) {
        quranDao.updateAyah(text, ayahId)
    }

    override suspend fun getRandomAyah(surahNumber: Int): Flow<Ayah> = flow {
        emit(quranDao.getRandomAyah(surahNumber).toDomain())
    }

    override fun getSurahInFrench(surahNumber: Long): Flow<List<Verse>> = flow {
        val response = api.getFrenchSurah(surahNumber)
        val surahInFrench = mutableListOf<Verse>()

        for (ayah in response.body()!!.data.verseResponse) {
            surahInFrench.add(ayah.toDomain())
        }
        emit(surahInFrench)
    }
}


