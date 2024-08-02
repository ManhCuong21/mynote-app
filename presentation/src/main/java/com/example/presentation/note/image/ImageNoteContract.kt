package com.example.presentation.note.image

import android.graphics.Bitmap
import androidx.fragment.app.FragmentActivity
import com.example.core.core.model.ItemImage

sealed interface ImageNoteAction {
    data class GetListImageNote(val context: FragmentActivity) : ImageNoteAction
    data class DeleteImageNote(val context: FragmentActivity, val imagePath: String) : ImageNoteAction
    data class SaveImageNote(val context: FragmentActivity, val bitmap: Bitmap) : ImageNoteAction
}

sealed interface ImageNoteSingleEvent {
    data class GetListImage(val list: List<ItemImage>) : ImageNoteSingleEvent
}