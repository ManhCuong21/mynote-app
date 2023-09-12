package com.example.presentation.dialog.datetimepickers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.core.base.BaseViewModel
import com.example.core.core.external.AppConstants
import com.example.core.core.external.ResultContent
import com.example.core.core.external.formatDate
import com.example.core.core.model.NoteModel
import com.example.core.core.model.NotificationModel
import com.example.domain.usecase.local.NoteUseCase
import com.github.michaelbull.result.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DateTimePickersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val noteUseCase: NoteUseCase
) : BaseViewModel() {
    private val _mutableStateFlow: MutableStateFlow<DateTimePickersUiState>
    val stateFlow: StateFlow<DateTimePickersUiState>

    private val _actionSharedFlow =
        MutableSharedFlow<DateTimePickersAction>(extraBufferCapacity = 64)

    private inline fun <reified T : DateTimePickersAction> action() =
        _actionSharedFlow.filterIsInstance<T>()

    private val _singleEventChannel =
        Channel<DateTimePickersSingleEvent>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<DateTimePickersSingleEvent> get() = _singleEventChannel.receiveAsFlow()

    fun dispatch(action: DateTimePickersAction) =
        viewModelScope.launch { _actionSharedFlow.emit(action) }

    init {
        val initialUiState = savedStateHandle.get<DateTimePickersUiState?>(STATE_KEY)?.copy()
            ?: DateTimePickersUiState.INITIAL
        _mutableStateFlow = MutableStateFlow(initialUiState).apply {
            onEach { savedStateHandle[STATE_KEY] = it }.launchIn(viewModelScope)
        }
        stateFlow = _mutableStateFlow.asStateFlow()

        updateDayOfMonth()
        updateDayOfWeek()
        saveNotification()
        cancelNotification()
    }

    private fun updateDayOfMonth() {
        action<DateTimePickersAction.UpdateDayOfMonth>()
            .onEach {
                _mutableStateFlow.update { state ->
                    state.copy(dayOfMonth = it.dayOfMonth, dayOfWeek = emptyList())
                }
                val textDate = if (!it.isDefault) {
                    it.dayOfMonth?.let { dayOfMonth ->
                        setupTextDateOfMonth(dayOfMonth)
                    }
                } else {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    setupTextDateOfMonth(calendar.timeInMillis)
                }
                _singleEventChannel.send(
                    DateTimePickersSingleEvent.UpdateTextNotification(textDate.orEmpty())
                )
            }.launchIn(viewModelScope)
    }

    private fun updateDayOfWeek() {
        action<DateTimePickersAction.UpdateDayOfWeek>()
            .map {
                if (it.dayOfWeek != null) {
                    var list = arrayListOf<Int>()
                    if (_mutableStateFlow.value.dayOfWeek.isNotEmpty()) {
                        list = _mutableStateFlow.value.dayOfWeek as ArrayList
                        if (list.contains(it.dayOfWeek)) {
                            list.remove(it.dayOfWeek)
                        } else list.add(it.dayOfWeek)
                    } else {
                        list.add(it.dayOfWeek)
                    }
                    list
                } else listOf()
            }
            .onEach {
                _mutableStateFlow.update { state ->
                    state.copy(dayOfMonth = null, dayOfWeek = it)
                }
                if (it.isNotEmpty()) {
                    val textDate = setupTextDayOfWeek(it)
                    _singleEventChannel.send(DateTimePickersSingleEvent.UpdateTextNotification(textDate))
                } else {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    dispatch(DateTimePickersAction.UpdateDayOfMonth(calendar.timeInMillis))
                }
            }.launchIn(viewModelScope)
    }

    private fun setupTextDayOfWeek(list: List<Int>): String {
        var textDate = ""
        list.forEach {
            when (it) {
                Calendar.MONDAY -> textDate = plusText(textDate, "Mon")
                Calendar.TUESDAY -> textDate = plusText(textDate, "Tue")
                Calendar.WEDNESDAY -> textDate = plusText(textDate, "Wed")
                Calendar.THURSDAY -> textDate = plusText(textDate, "Thu")
                Calendar.FRIDAY -> textDate = plusText(textDate, "Fri")
                Calendar.SATURDAY -> textDate = plusText(textDate, "Sat")
                Calendar.SUNDAY -> textDate = plusText(textDate, "Sun")
            }
        }
        return textDate
    }

    private fun plusText(oldText: String, text: String): String {
        return if (oldText.isEmpty()) {
            oldText.plus("Every $text")
        } else oldText.plus(",$text")
    }

    private fun setupTextDateOfMonth(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        val calendarToday = Calendar.getInstance()
        val calendarNextDay = Calendar.getInstance()
        calendarNextDay.add(Calendar.DAY_OF_YEAR, 1)
        val textDate = when (calendar.get(Calendar.DAY_OF_YEAR)) {
            calendarToday.get(Calendar.DAY_OF_YEAR) -> "Today - " +
                    formatDate(AppConstants.FORMAT_TIME_DEFAULT_NOTIFICATION, date)

            calendarNextDay.get(Calendar.DAY_OF_YEAR) -> "Tomorrow - " +
                    formatDate(AppConstants.FORMAT_TIME_DEFAULT_NOTIFICATION, date)

            else -> formatDate(AppConstants.FORMAT_TIME_DEFAULT_NOTIFICATION, date)
        }
        return textDate.orEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun saveNotification() {
        action<DateTimePickersAction.SaveNotification>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    noteUseCase.updateNotificationNote(
                        NoteModel(
                            idNote = it.noteModel.idNote,
                            titleNote = it.noteModel.titleNote,
                            contentNote = it.noteModel.contentNote,
                            categoryNote = it.noteModel.categoryNote,
                            nameMediaNote = it.noteModel.nameMediaNote,
                            hasImage = it.noteModel.hasImage,
                            hasRecord = it.noteModel.hasRecord,
                            colorTitleNote = it.noteModel.colorTitleNote,
                            colorContentNote = it.noteModel.colorContentNote,
                            timeNote = it.noteModel.timeNote,
                            notificationModel = NotificationModel(
                                idNotification = it.noteModel.idNote,
                                dayOfMonth = stateFlow.value.dayOfMonth,
                                dayOfWeek = stateFlow.value.dayOfWeek,
                                hour = it.hour,
                                minute = it.minute
                            )
                        )
                    ).fold(
                        success = {
                            ResultContent.Content(it)
                        },
                        failure = {
                            ResultContent.Error(it)
                        }
                    ).let { emit(it) }
                }
            }.onEach { lce ->
                val event = when (lce) {
                    is ResultContent.Loading -> null
                    is ResultContent.Content -> DateTimePickersSingleEvent.SaveNotification.Success
                    is ResultContent.Error -> DateTimePickersSingleEvent.SaveNotification.Failed(
                        error = lce.error
                    )
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun cancelNotification() {
        action<DateTimePickersAction.CancelNotification>()
            .flatMapLatest {
                flow {
                    emit(ResultContent.Loading)
                    noteUseCase.updateNotificationNote(
                        NoteModel(
                            idNote = it.noteModel.idNote,
                            titleNote = it.noteModel.titleNote,
                            contentNote = it.noteModel.contentNote,
                            categoryNote = it.noteModel.categoryNote,
                            nameMediaNote = it.noteModel.nameMediaNote,
                            hasImage = it.noteModel.hasImage,
                            hasRecord = it.noteModel.hasRecord,
                            colorTitleNote = it.noteModel.colorTitleNote,
                            colorContentNote = it.noteModel.colorContentNote,
                            timeNote = it.noteModel.timeNote,
                            notificationModel = null
                        )
                    ).fold(
                        success = {
                            ResultContent.Content(it)
                        },
                        failure = {
                            ResultContent.Error(it)
                        }
                    ).let { emit(it) }
                }
            }.onEach { lce ->
                val event = when (lce) {
                    is ResultContent.Loading -> null
                    is ResultContent.Content -> DateTimePickersSingleEvent.SaveNotification.Cancel
                    is ResultContent.Error -> DateTimePickersSingleEvent.SaveNotification.Failed(
                        error = lce.error
                    )
                }
                event?.let { _singleEventChannel.send(it) }
            }.launchIn(viewModelScope)
    }

    private companion object {
        private const val STATE_KEY = "DateTimePickersViewModel.state"
    }
}