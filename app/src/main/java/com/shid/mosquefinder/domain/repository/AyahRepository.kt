package com.shid.mosquefinder.domain.repository

import com.shid.mosquefinder.domain.model.Ayah
import kotlinx.coroutines.flow.Flow

interface AyahRepository {

    fun getAyah(surahNumber: Int): List<Ayah>

    fun updateAyah(text: String, ayahId: Long)

    fun getRandomAyah(surahNumber: Int): Ayah
}