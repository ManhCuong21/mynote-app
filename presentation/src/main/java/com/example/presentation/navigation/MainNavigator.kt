package com.example.presentation.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import com.example.core.core.model.CategoryUIModel
import com.example.core.core.model.NoteUIModel

@MainThread
interface MainNavigator {
    val navController: NavController

    sealed interface Direction {
        data class MainFragmentToAddNoteFragment(val category: CategoryUIModel) : Direction
        data class MainFragmentToUpdateNoteFragment(val noteModel: NoteUIModel) : Direction
        object MainFragmentToAddCategoryFragment : Direction
        data class MainFragmentToUpdateCategoryFragment(val category: CategoryUIModel) : Direction
        data class NoteFragmentToRecorderFragment(val fileMediaName: String) : Direction
    }

    @MainThread
    fun navigate(direction: Direction)

    @MainThread
    fun popBackStack()
}