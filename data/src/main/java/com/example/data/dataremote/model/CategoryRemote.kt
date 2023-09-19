package com.example.data.dataremote.model

import com.example.core.core.external.AppConstants.TYPE_REMOTE
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class CategoryRemote(
    val idCategory: Long = System.currentTimeMillis(),
    val titleCategory: String? = null,
    val imageCategory: Int? = null,
    val typeCategory: Int = TYPE_REMOTE
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "idCategory" to idCategory,
            "titleCategory" to titleCategory,
            "imageCategory" to imageCategory,
            "typeCategory" to typeCategory
        )
    }
}