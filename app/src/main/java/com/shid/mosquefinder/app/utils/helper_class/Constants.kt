package com.shid.mosquefinder.app.utils.helper_class

import com.shid.mosquefinder.app.ui.main.views.HomeActivity

object Constants {

    //Blog fields
    const val BLOG_COLLECTION_PATH = "blog"
    const val BLOG_FIELD_TITLE = "title"
    const val BLOG_FIELD_TITLE_FR = "title_fr"
    const val BLOG_FIELD_AUTHOR = "author"
    const val BLOG_FIELD_BODY = "body"
    const val BLOG_FIELD_IMAGE = "image_link"
    const val BLOG_FIELD_BODY_FR = "body_fr"
    const val BLOG_FIELD_TAG = "tag"

    //Json fields
    const val SURAHS = "surahs"
    const val SURAH_NUMBER = "number"
    const val SURAH_NAME = "name"
    const val SURAH_TRANSLITERATION_EN = "transliteration_en"
    const val SURAH_TRANSLATION_EN = "translation_en"
    const val SURAH_TOTAL_VERSES = "total_verses"
    const val SURAH_REVELATION_TYPE = "revelation_type"

    const val CATEGORIES = "categories"
    const val CATEGORY_NAME = "category_name"
    const val CHAPTERS = "chapters"
    const val CHAPTER_NAME = "chapter_name"
    const val CATEGORY_ID = "category_id"
    const val DIVINE_NAMES = "noms"
    const val DIVINE_NAME = "name"
    const val AYAHS = "ayahs"
    const val AYAH_SURAH_NUMBER = "surah_number"
    const val AYAH_NUMBER = "ayahs"
    const val AYAH_TEXT = "ayahs"
    const val AYAH_TRANSLATION = "ayahs"

    //Intent extras
    const val EXTRA_SURAH_NUMBER = "surah_number"

    //Intent keys
    const val EXTRA_KEY_MESSAGE = "message"

    //Intent Filters
    const val INTENT_FILTER_MESSAGE_RECEIVER = "MyData"

    //Surah ViewModel
    const val PRAYER_NOW = "Now"
    const val PRAYER_TIME = "It's time to pray"

    //Workers
    const val WORKER_TAG = "notification"
    const val WORKER_NAME = "Daily Ayah"


}