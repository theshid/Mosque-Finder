package com.shid.mosquefinder.data.repository

import com.shid.mosquefinder.data.api.QuranApiInterface
import com.shid.mosquefinder.data.local.database.QuranDao
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

    override fun getRandomAyah(surahNumber: Int): Ayah {
        return quranDao.randomAyah.toDomain()
    }

    override fun getSurahInFrench(surahNumber: Int): Flow<List<Verse>> = flow {
        val response = api.getFrenchSurah(surahNumber)
        val surahInFrench = mutableListOf<Verse>()

        for (ayah in response.body()!!.data.verseResponse) {
            surahInFrench.add(ayah.toDomain())
        }
        emit(surahInFrench)
    }
}


