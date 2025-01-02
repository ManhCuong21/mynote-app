package com.example.presentation.navigation

import android.app.Activity
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.example.core.core.external.ActionCategory
import com.example.core.core.external.ActionNote
import com.example.core.core.external.checkMainThread
import com.example.core.core.external.safeNavigate
import javax.inject.Inject
import com.example.presentation.R
import com.example.presentation.authentication.signin.SignInFragmentDirections
import com.example.presentation.note.NoteFragmentDirections
import com.example.presentation.main.MainFragmentDirections
import com.example.presentation.main.setting.security.SecurityFragmentDirections
import com.example.presentation.navigation.MainNavigator.Direction

class MainNavigatorImpl @Inject constructor(
    private val activity: Activity
) : MainNavigator {
    override val navController: NavController
        get() = activity.findNavController(R.id.nav_host_fragment_activity_main)

    init {
        checkMainThread()
    }

    override fun navigate(direction: Direction) {
        checkMainThread()
        navController.safeNavigate { navigate(direction.toNavDirections()) }
    }

    override fun popBackStack() {
        checkMainThread()
        navController.safeNavigate { popBackStack() }
    }
}


private fun Direction.toNavDirections(): NavDirections = when (this) {
    is Direction.SignInFragmentToSignUpFragment -> SignInFragmentDirections.actionSignInFragmentToSignUpFragment()
    is Direction.MainFragmentToSignInFragment -> MainFragmentDirections.actionMainFragmentToSignInFragment()
    is Direction.MainFragmentToUserInformationFragment -> MainFragmentDirections.actionMainFragmentToUserInformationFragment()
    is Direction.MainFragmentToAddNoteFragment -> MainFragmentDirections.actionMainFragmentToNoteFragment(
        actionNote = ActionNote.INSERT_NOTE,
        category = category,
        noteModel = null
    )

    is Direction.MainFragmentToUpdateNoteFragment -> MainFragmentDirections.actionMainFragmentToNoteFragment(
        actionNote = ActionNote.UPDATE_NOTE,
        category = null,
        noteModel = noteModel
    )

    is Direction.MainFragmentToAddCategoryFragment -> MainFragmentDirections.actionMainFragmentToCategoryFragment(
        actionCategory = ActionCategory.INSERT_CATEGORY,
        category = null
    )

    is Direction.MainFragmentToUpdateCategoryFragment -> MainFragmentDirections.actionMainFragmentToCategoryFragment(
        actionCategory = ActionCategory.UPDATE_CATEGORY,
        category = category
    )

    is Direction.MainFragmentToDateTimePickersFragment -> MainFragmentDirections.actionMainFragmentToDateTimePickersFragment(
        noteModel = noteModel
    )

    is Direction.NoteFragmentToImageNoteFragment -> NoteFragmentDirections.actionNoteFragmentToImageNoteFragment()
    is Direction.NoteFragmentToRecorderFragment -> NoteFragmentDirections.actionNoteFragmentToRecorderFragment()
    is Direction.MainFragmentToSecurityFragment -> MainFragmentDirections.actionMainFragmentToSecurityFragment()
    is Direction.SecurityFragmentToChangeUnlockCodeFragment -> SecurityFragmentDirections.actionChangeUnlockCodeFragmentToChangeUnlockCodeFragment()
}