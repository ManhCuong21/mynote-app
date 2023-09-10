package com.example.presentation.note

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.fragment.app.FragmentActivity
import com.example.core.core.model.CategoryModel
import com.example.core.core.model.ItemImage
import com.example.core.core.model.ItemRecord
import com.example.core.core.model.NoteModel
import com.example.core.core.model.NotificationModel
import kotlinx.parcelize.Parcelize

sealed interface NoteAction {
    data class DeleteDirectory(val context: FragmentActivity) : NoteAction
    data class DeleteDirectoryTemp(val context: FragmentActivity) : NoteAction
    data class TitleNoteChanged(val titleNote: String) : NoteAction
    data class ContentNoteChanged(val contentNote: String) : NoteAction
    data class CategoryNoteChanged(val categoryNote: CategoryModel) : NoteAction
    data class DirectoryNameNoteChanged(val fileMediaNote: String) : NoteAction
    data class HasImageNoteChanged(val hasImage: Boolean) : NoteAction
    data class HasRecordNoteChanged(val hasRecord: Boolean) : NoteAction
    data class SaveImageNote(val context: FragmentActivity, val bitmap: Bitmap) : NoteAction
    data class GetListImageNote(val context: FragmentActivity) : NoteAction
    data class DeleteImageNote(val context: FragmentActivity, val pathImage: String) : NoteAction
    data class GetListRecordNote(val context: FragmentActivity) : NoteAction
    data class DeleteRecordNote(val context: FragmentActivity, val pathRecord: String) : NoteAction
    data class ColorTitleNoteChanged(val colorTitleNote: String) : NoteAction
    data class ColorContentNoteChanged(val colorContentNote: String) : NoteAction
    data class NotificationNoteChanged(val notificationModel: NotificationModel?) : NoteAction
    data class SaveMediaToDirectory(val context: FragmentActivity) : NoteAction
    data class SaveFileMediaToTemp(val context: FragmentActivity, val directoryName: String) :
        NoteAction

    data class InsertNote(val context: FragmentActivity) : NoteAction
    data class UpdateNote(val noteModel: NoteModel) : NoteAction
}

sealed interface NoteSingleEvent {
    data class GetListImage(val list: List<ItemImage>) : NoteSingleEvent
    data class GetListRecord(val list: List<ItemRecord>) : NoteSingleEvent
    sealed interface SaveNote : NoteSingleEvent {
        object Success : SaveNote
        data class Failed(val error: Throwable) : SaveNote
    }
}

@Parcelize
data class NoteUiState(
    val titleNote: String?,
    val contentNote: String?,
    val categoryNote: CategoryModel,
    val directoryName: String?,
    val hasImage: Boolean?,
    val hasRecord: Boolean?,
    val colorTitleNote: String?,
    val colorContentNote: String?,
    val notificationModel: NotificationModel?
) : Parcelable {
    companion object {
        val INITIAL = NoteUiState(
            titleNote = null,
            contentNote = null,
            categoryNote = CategoryModel(-1, "", 0),
            directoryName = null,
            hasImage = null,
            hasRecord = null,
            colorTitleNote = null,
            colorContentNote = null,
            notificationModel = null
        )
    }
}

fun buildNoteUiState(
    titleNote: String,
    contentNote: String,
    categoryNote: CategoryModel,
    directoryName: String,
    hasImage: Boolean,
    hasRecord: Boolean,
    colorTitleNote: String?,
    colorContentNote: String?,
    notificationModel: NotificationModel?
): NoteUiState = NoteUiState(
    titleNote = titleNote,
    contentNote = contentNote,
    categoryNote = categoryNote,
    directoryName = directoryName,
    hasImage = hasImage,
    hasRecord = hasRecord,
    colorTitleNote = colorTitleNote,
    colorContentNote = colorContentNote,
    notificationModel = notificationModel
)