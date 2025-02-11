package com.example.presentation.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import com.example.core.core.model.CategoryModel
import com.example.core.core.model.NoteModel
import com.example.presentation.main.setting.security.manager.AuthMethod

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
        data object NoteFragmentToImageNoteFragment : Direction
        data object NoteFragmentToRecorderFragment : Direction
        data object MainFragmentToSecurityFragment : Direction
        data class SecurityFragmentToSetupUnlockCodeFragment(val authMethod: AuthMethod) : Direction
        data class SetupUnlockCodeFragmentToSecondSetupUnlockCodeFragment(
            val authMethod: AuthMethod,
            val isSecondAttempt: Boolean,
            val firstOtp: String
        ) : Direction
    }

    @MainThread
    fun navigate(direction: Direction)

    @MainThread
    fun popBackStack()
}