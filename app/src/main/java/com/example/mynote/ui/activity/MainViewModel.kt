package com.example.mynote.ui.activity

import com.example.mynote.base.BaseViewModel
import com.example.mynote.ui.usecase.NoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val noteUseCase: NoteUseCase
) : BaseViewModel() {

}