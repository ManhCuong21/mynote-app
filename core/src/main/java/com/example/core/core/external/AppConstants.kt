package com.example.core.core.external

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

object AppConstants {
    const val PATH_MEDIA_NOTE = "MediaNote"
    const val FILE_NAME_FORMAT = "yyyyMMddHHmmss"
    const val FORMAT_TIME_MINUTE = "mm:ss"

    const val DATE_FORMAT_TIME_12_HOUR = "yyyy-MM-dd | hh:mm:ss aa"
    const val DATE_FORMAT_TIME_24_HOUR = "yyyy-MM-dd | HH:mm:ss"
}

@Parcelize
enum class ActionNote : Parcelable {
    INSERT_NOTE,
    SHOW_ON_MAP,
    UPDATE_NOTE,
    CHANGE_CATEGORY,
    DELETE_NOTE
}