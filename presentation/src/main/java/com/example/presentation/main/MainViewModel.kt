package com.example.presentation.main

import com.example.core.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class MainViewModel @Inject constructor() : BaseViewModel() {
    val indexBottomNav = MutableStateFlow(0)
}