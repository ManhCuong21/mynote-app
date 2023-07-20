package com.example.mynote.ui.addnote.record

import androidx.lifecycle.viewModelScope
import com.example.mynote.base.BaseViewModel
import com.example.mynote.core.external.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RecorderViewModel @Inject constructor() : BaseViewModel() {
    private var time = MutableStateFlow(0L)
    private var isRunning = MutableStateFlow(false)
    private val _singleEventChannel = Channel<String>(Channel.UNLIMITED).addToBag()
    val singleEventFlow: Flow<String> get() = _singleEventChannel.receiveAsFlow()

    fun runTime() {
        isRunning.value = true
        flowTime()
    }

    fun stopTime() {
        isRunning.value = false
        flowTime()
    }

    private fun flowTime() {
        flow {
            while (isRunning.value) {
                time.value += 1000
                delay(1000)
                emit(time.value)
            }
        }.map {
            val currentTime = time.value
            val simpleDateFormat =
                SimpleDateFormat(AppConstants.FORMAT_TIME_MINUTE, Locale.getDefault())
            if (currentTime == 0L) {
                ""
            } else {
                val dateTime = Calendar.getInstance().apply { timeInMillis = currentTime }
                simpleDateFormat.format(dateTime.time)
            }
        }.onEach { _singleEventChannel.send(it) }.launchIn(viewModelScope)
    }
}