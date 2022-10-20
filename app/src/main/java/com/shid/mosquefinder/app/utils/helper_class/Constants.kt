package com.shid.mosquefinder.app.utils.helper_class

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
    const val EXTRA_SURAH_NAME = "surah"
    const val EXTRA_SURAH_LINK = "link"
    const val EXTRA_SURAH_NUMBER_AYAH = "number"
    const val EXTRA_SURAH_NAME_AYAH = "surah_name"
    const val EXTRA_STATE_PLAYER = "state_player"


    //Intent keys
    const val EXTRA_KEY_MESSAGE = "message"

    //Bundles
    const val BUNDLE_KEY_DURATION = "duration"
    const val BUNDLE_KEY_CURRENT_POSITION = "current_position"
    const val BUNDLE_KEY_PLAYER_STATUS = "play_status"

    //Intent Filters
    const val INTENT_FILTER_MESSAGE_RECEIVER = "MyData"

    //Surah ViewModel
    const val PRAYER_NOW = "Now"
    const val PRAYER_TIME = "It's time to pray"

    //AyahActivity
    const val SURAH_STATE = "state"

    //Workers
    const val WORKER_TAG = "notification"
    const val WORKER_NAME = "Daily Ayah"

    //Version
    const val FRENCH_VERSION = "fr"

    //ExoPlayer
    const val PLAYER_TYPE = "default"

    //Service
    const val QARI_MISHARY = "Mishary bin Rashid Alafasy"

    //Player Command
    const val COMMAND_SEEK = "seek"
    const val COMMAND_REPEAT = "repeat"
    const val COMMAND_SURAH = "surah"
    const val EVENT_MEDIA_INFORMATION = "player_information"
    const val EVENT_PLAYER_PAUSE = "play_pause"
    const val EVENT_PLAYER_FINISH = "finish"


}