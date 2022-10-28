package com.shid.mosquefinder.app.store

import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalSerializationApi::class)
@Singleton
internal class CrawlerStateSerializer @Inject constructor(private val json: Json) :
    Serializer<CrawlerState> {
    override val defaultValue: CrawlerState = CrawlerState()

    override suspend fun readFrom(input: InputStream): CrawlerState =
        json.decodeFromStream(CrawlerState.serializer(), input)

    override suspend fun writeTo(t: CrawlerState, output: OutputStream) {
        json.encodeToStream(CrawlerState.serializer(), t, output)
    }
}