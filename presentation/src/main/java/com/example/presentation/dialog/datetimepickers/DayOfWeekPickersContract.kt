package com.example.presentation.dialog.datetimepickers

import android.os.Parcelable
import com.example.core.core.model.NoteModel
import kotlinx.parcelize.Parcelize

sealed interface DateTimePickersAction {
    data class UpdateDayOfMonth(val dayOfMonth: Long?, val isDefault: Boolean = false) :
        DateTimePickersAction

    data class UpdateDayOfWeek(val dayOfWeek: Int?) : DateTimePickersAction
    data class SaveNotification(val noteModel: NoteModel, val hour: Int, val minute: Int) :
        DateTimePickersAction

    data class CancelNotification(val noteModel: NoteModel) : DateTimePickersAction
}

sealed interface DateTimePickersSingleEvent {
    data class UpdateTextNotification(val text: String) : DateTimePickersSingleEvent
    sealed interface SaveNotification : DateTimePickersSingleEvent {
        object Success : SaveNotification
        object Cancel : SaveNotification
        data class Failed(val error: Throwable) : SaveNotification
    }
}

@Parcelize
data class DateTimePickersUiState(
    val dayOfMonth: Long?,
    val dayOfWeek: List<Int>
) : Parcelable {
    companion object {
        val INITIAL = DateTimePickersUiState(
            dayOfMonth = null,
            dayOfWeek = emptyList()
        )
    }
}