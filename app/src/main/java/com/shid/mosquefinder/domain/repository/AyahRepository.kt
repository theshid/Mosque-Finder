package com.shid.mosquefinder.domain.repository

import com.shid.mosquefinder.data.local.database.entities.Ayah

interface AyahRepository {

    fun getAyah(surahNumber: Int): List<Ayah>

    fun updateAyah(text:String,ayahId:Long)

    fun getRandomAyah(surahNumber: Int)
}