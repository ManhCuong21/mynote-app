package com.example.data.dataremote.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class NoteRemote(
    val idNote: Int = 0,
    var idCategory: Long,
    var titleCategory: String,
    var imageCategory: Int,
    val titleNote: String,
    val contentNote: String,
    val fileMediaNote: String,
    val hasImage: Boolean,
    val hasRecord: Boolean,
    val colorTitleNote: String,
    val colorContentNote: String,
    val timeNote: Long,
    val idNotification: Int? = null,
    val dayOfMonth: Long? = null,
    val dayOfWeek: List<Int>? = null,
    val hour: Int? = null,
    val minute: Int? = null
)