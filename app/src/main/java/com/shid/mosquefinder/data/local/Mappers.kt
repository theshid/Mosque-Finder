package com.shid.mosquefinder.data.local

import com.shid.mosquefinder.data.local.database.entities.AyahDb
import com.shid.mosquefinder.domain.model.Ayah

internal fun AyahDb.toDomain()=
     Ayah(id,surah_number,verse_number,originalText,translation, frenchTranslation!!)