package com.shid.mosquefinder.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ayahs")
data class AyahDb(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "surah_number") val surah_number: Int,
    @ColumnInfo(name = "verse_number") val verse_number: Int,
    @ColumnInfo(name = "text") val originalText: String,
    @ColumnInfo(name = "translation") val translation: String,
    @ColumnInfo(name = "french_text") var frenchTranslation: String? = "empty"
)
