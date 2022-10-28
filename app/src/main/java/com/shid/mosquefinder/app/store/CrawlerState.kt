package com.shid.mosquefinder.app.store

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CrawlerState(
    @SerialName("lastSurahId") val lastSurahId: Long = -1,
    @SerialName("ignored") val ignored: List<Long> = listOf()
)
