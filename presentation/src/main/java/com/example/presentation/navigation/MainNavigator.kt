package com.example.presentation.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import com.example.core.core.model.CategoryModel
import com.example.core.core.model.NoteModel

@MainThread
interface MainNavigator {
    val navController: NavController

    sealed interface Direction {
        data class MainFragmentToAddNoteFragment(val category: CategoryModel) : Direction
        data class MainFragmentToUpdateNoteFragment(val noteModel: NoteModel) : Direction
        object MainFragmentToAddCategoryFragment : Direction
        data class MainFragmentToUpdateCategoryFragment(val category: CategoryModel) : Direction
        data class MainFragmentToDateTimePickersFragment(val noteModel: NoteModel) : Direction
        data class NoteFragmentToRecorderFragment(val fileMediaName: String) : Direction
    }

    @MainThread
    fun navigate(direction: Direction)

    @MainThread
    fun popBackStack()
}