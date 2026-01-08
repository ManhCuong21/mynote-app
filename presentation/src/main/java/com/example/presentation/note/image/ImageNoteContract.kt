package com.example.presentation.note.image

import android.graphics.Bitmap
import com.example.core.core.model.ItemImage

sealed interface ImageNoteAction {
    data object GetListImageNote : ImageNoteAction
    data class DeleteImageNote(val imagePath: String) : ImageNoteAction
    data class SaveImageNote(val bitmap: Bitmap) : ImageNoteAction
}

sealed interface ImageNoteSingleEvent {
    data class GetListImage(val list: List<ItemImage>) : ImageNoteSingleEvent
}