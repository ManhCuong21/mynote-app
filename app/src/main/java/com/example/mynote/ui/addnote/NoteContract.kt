package com.example.mynote.ui.addnote

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface NoteAction {
    data class CameraPermissionResult(val isGranted: Boolean) : NoteAction
    data class StoragePermissionResult(val isGranted: Boolean) : NoteAction
    object UpdateListImage : NoteAction
}

sealed interface NoteSingleEvent {
    object UpdateListImage : NoteSingleEvent
}

@Parcelize
data class NoteUiState(
    val permissionCameraGranted: Boolean,
    val permissionStorageGranted: Boolean,
) : Parcelable {
    companion object {
        val INITIAL = NoteUiState(
            permissionCameraGranted = false,
            permissionStorageGranted = false,
        )
    }
}

fun buildNoteUiState(
    permissionCameraGranted: Boolean,
    permissionStorageGranted: Boolean,
): NoteUiState = NoteUiState(
    permissionCameraGranted = permissionCameraGranted,
    permissionStorageGranted = permissionStorageGranted,
)

data class ItemChooseColor(val colorTitle: Int, val colorContent: Int)