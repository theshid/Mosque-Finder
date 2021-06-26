package com.shid.mosquefinder.Data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Item(
    @PrimaryKey(autoGenerate = true)  val id:Long,
    @ColumnInfo(name = "item_translation") val itemTranslation: String,
    @ColumnInfo(name = "chapter_name") val chapterName: String,
)
