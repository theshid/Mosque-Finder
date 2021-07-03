package com.shid.mosquefinder.Data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item")
data class Item(
    @PrimaryKey(autoGenerate = true)  val id:Int,
    @ColumnInfo(name = "item_translation") val itemTranslation: String,
    @ColumnInfo(name = "chapter_id") val chapterId: Int,
)
