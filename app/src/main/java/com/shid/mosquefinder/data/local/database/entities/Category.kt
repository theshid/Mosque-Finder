package com.shid.mosquefinder.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "category_name") val categoryName: String,
)
