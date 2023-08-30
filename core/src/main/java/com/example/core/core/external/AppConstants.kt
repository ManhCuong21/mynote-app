package com.example.core.core.external

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

object AppConstants {
    const val PATH_MEDIA_NOTE = "MediaNote"
    const val FILE_NAME_FORMAT = "yyyyMMddHHmmss"
    const val FORMAT_TIME_MINUTE = "mm:ss"
    const val FORMAT_TIME_DEFAULT_NOTIFICATION = "EEE, MMM dd"

    const val DATE_FORMAT_TIME_12_HOUR = "yyyy-MM-dd | hh:mm:ss aa"
    const val DATE_FORMAT_TIME_24_HOUR = "yyyy-MM-dd | HH:mm:ss"

    const val KEY_CHANNEL_ID_NOTIFICATION = "channelIdNotification"
    const val KEY_TITLE_NOTIFICATION = "titleNotification"
    const val KEY_CONTENT_NOTIFICATION = "contentNotification"
}

@Parcelize
enum class ActionNote : Parcelable {
    INSERT_NOTE,
    NOTIFICATION,
    UPDATE_NOTE,
    CHANGE_CATEGORY,
    DELETE_NOTE
}

@Parcelize
enum class ActionCategory : Parcelable {
    INSERT_CATEGORY,
    UPDATE_CATEGORY,
    DELETE_CATEGORY
}