package com.example.presentation.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import com.example.core.core.model.CategoryModel
import com.example.core.core.model.NoteModel
import com.example.presentation.biometric.AuthMethod

@MainThread
interface MainNavigator {
    val navController: NavController

    sealed interface Direction {
        data object MainFragmentToUserInformationFragment : Direction
        data class MainFragmentToAddNoteFragment(val category: CategoryModel) : Direction
        data class MainFragmentToUpdateNoteFragment(val noteModel: NoteModel) : Direction
        data object MainFragmentToAddCategoryFragment : Direction
        data class MainFragmentToUpdateCategoryFragment(val category: CategoryModel) : Direction
        data class MainFragmentToDateTimePickersFragment(val noteModel: NoteModel) : Direction
        data object MainFragmentToCompassFragment : Direction
        data object NoteFragmentToImageNoteFragment : Direction
        data object NoteFragmentToRecorderFragment : Direction
        data object NoteFragmentToSecurityFragment : Direction
        data object CategoryFragmentToSecurityFragment : Direction
        data object MainFragmentToSecurityFragment : Direction
        data class ImageNoteFragmentToEditImageNoteFragment(val imagePath: String?) : Direction
        data object MainFragmentToPrivacyPolicyFragment : Direction
        data class SecurityFragmentToSetupUnlockCodeFragment(val authMethod: AuthMethod) : Direction
        data class SetupUnlockCodeFragmentToSecondSetupUnlockCodeFragment(
            val authMethod: AuthMethod,
            val isSecondInput: Boolean,
            val isAfterConfirm: Boolean = false,
            val firstOtp: String?
        ) : Direction
    }

    @MainThread
    fun navigate(direction: Direction)

    @MainThread
    fun popBackStack()
}