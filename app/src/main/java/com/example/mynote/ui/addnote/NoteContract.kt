package com.example.mynote.ui.addnote

import android.os.Parcelable
import android.provider.ContactsContract.CommonDataKinds.Note
import kotlinx.parcelize.Parcelize

sealed interface NoteAction {
    data class CameraPermissionResult(val isGranted: Boolean) : NoteAction
    data class StoragePermissionResult(val isGranted: Boolean) : NoteAction
    data class UpdateListImage(val image: String) : NoteAction
}

sealed interface NoteSingleEvent {

}

@Parcelize
data class NoteUiState(
    val permissionCameraGranted: Boolean,
    val permissionStorageGranted: Boolean,
    val listImage: List<String>
) : Parcelable {
    companion object {
        val INITIAL = NoteUiState(
            permissionCameraGranted = false,
            permissionStorageGranted = false,
            listImage = arrayListOf()
        )
    }
}

fun buildNoteUiState(
    permissionCameraGranted: Boolean,
    permissionStorageGranted: Boolean,
    listImage: List<String>
): NoteUiState = NoteUiState(
    permissionCameraGranted = permissionCameraGranted,
    permissionStorageGranted = permissionStorageGranted,
    listImage = listImage
)

data class ItemChooseColor(val colorTitle: Int, val colorContent: Int)