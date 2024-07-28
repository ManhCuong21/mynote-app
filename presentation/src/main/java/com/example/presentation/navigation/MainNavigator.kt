package com.example.presentation.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import com.example.core.core.model.CategoryModel
import com.example.core.core.model.NoteModel

@MainThread
interface MainNavigator {
    val navController: NavController

    sealed interface Direction {
        data object SignInFragmentToSignUpFragment : Direction
        data object MainFragmentToSignInFragment : Direction
        data object MainFragmentToUserInformationFragment : Direction
        data class MainFragmentToAddNoteFragment(val category: CategoryModel) : Direction
        data class MainFragmentToUpdateNoteFragment(val noteModel: NoteModel) : Direction
        data object MainFragmentToAddCategoryFragment : Direction
        data class MainFragmentToUpdateCategoryFragment(val category: CategoryModel) : Direction
        data class MainFragmentToDateTimePickersFragment(val noteModel: NoteModel) : Direction
        data object NoteFragmentToRecorderFragment : Direction
    }

    @MainThread
    fun navigate(direction: Direction)

    @MainThread
    fun popBackStack()
}