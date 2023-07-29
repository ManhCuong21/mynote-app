package com.example.presentation.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import com.example.core.core.model.CategoryUIModel

@MainThread
interface MainNavigator {
    val navController: NavController

    sealed interface Direction {
        data class MainFragmentToNoteFragment(val category: CategoryUIModel) : Direction
        data class NoteFragmentToRecorderFragment(val fileMediaName: String) : Direction
    }

    @MainThread
    fun navigate(direction: Direction)

    @MainThread
    fun popBackStack()
}