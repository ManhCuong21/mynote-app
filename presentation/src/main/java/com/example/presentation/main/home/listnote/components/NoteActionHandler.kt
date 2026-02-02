package com.example.presentation.main.home.listnote.components

import com.example.core.core.external.ActionNote
import com.example.core.core.model.NoteModel

interface NoteActionHandler {
    fun onHandleAction(action: ActionNote, note: NoteModel)
    fun getSettings(): Pair<Boolean, Boolean> // format24Hour, isBiometric
}