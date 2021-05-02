package com.shid.mosquefinder.Data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ayahs")
data class Ayah(
    @PrimaryKey(autoGenerate = true)  val id:Long,
    @ColumnInfo(name = "surah_number") val surah_number: Int,
    @ColumnInfo(name = "verse_number") val name: Int,
    @ColumnInfo(name = "text") val transliteration: String,
    @ColumnInfo(name = "translation") val translation: String
)
