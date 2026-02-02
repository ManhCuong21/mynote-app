package com.example.presentation.dialog.di

import com.example.presentation.dialog.list.ListDialogFragment
import com.example.presentation.dialog.text.TextDialogFragment
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DialogService @Inject constructor() {
    private val _dialogEvent = MutableSharedFlow<DialogType>(extraBufferCapacity = 1)
    val dialogEvent = _dialogEvent.asSharedFlow()

    fun showText(init: TextDialogFragment.Builder.() -> Unit) {
        _dialogEvent.tryEmit(DialogType.Text(TextDialogFragment.Builder().apply(init)))
    }

    fun showList(init: ListDialogFragment.Builder.() -> Unit) {
        _dialogEvent.tryEmit(DialogType.List(ListDialogFragment.Builder().apply(init)))
    }
}

sealed class DialogType {
    data class Text(val builder: TextDialogFragment.Builder) : DialogType()
    data class List(val builder: ListDialogFragment.Builder) : DialogType()
}