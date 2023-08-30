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
    data class UpdateTextDayOfMonth(val dateOfMonth: Long) : DateTimePickersSingleEvent
    data class UpdateTextDayOfWeek(val dayOfWeek: List<Int>) : DateTimePickersSingleEvent
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

fun buildDateTimePickersUiState(
    dayOfMonth: Long?,
    dayOfWeek: List<Int>
): DateTimePickersUiState = DateTimePickersUiState(
    dayOfMonth = dayOfMonth,
    dayOfWeek = dayOfWeek
)