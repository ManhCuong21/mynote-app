package com.example.data.dataremote.model

import com.example.core.core.external.AppConstants.TYPE_REMOTE
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class NoteRemote(
    val categoryRemote: CategoryRemote? = null,
    // Note
    val idNote: Long = System.currentTimeMillis(),
    val titleNote: String? = null,
    val contentNote: String? = null,
    val fileMediaNote: String? = null,
    val hasImage: Boolean? = null,
    val hasRecord: Boolean? = null,
    val colorTitleNote: String? = null,
    val colorContentNote: String? = null,
    val timeNote: Long? = null,
    val typeNote: Int = TYPE_REMOTE,
    val notificationRemote: NotificationRemote? = null,
    val security: Boolean
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "idNote" to idNote,
            "categoryRemote" to categoryRemote,
            "titleNote" to titleNote,
            "contentNote" to contentNote,
            "fileMediaNote" to fileMediaNote,
            "hasImage" to hasImage,
            "hasRecord" to hasRecord,
            "colorTitleNote" to colorTitleNote,
            "colorContentNote" to colorContentNote,
            "timeNote" to timeNote,
            "typeNote" to typeNote,
            "notificationRemote" to notificationRemote,
            "security" to security
        )
    }
}