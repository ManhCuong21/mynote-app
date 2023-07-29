package com.example.presentation.navigation

import android.app.Activity
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.example.core.core.external.checkMainThread
import com.example.core.core.external.safeNavigate
import javax.inject.Inject
import com.example.presentation.R
import com.example.presentation.note.NoteFragmentDirections
import com.example.presentation.main.MainFragmentDirections
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
    is Direction.MainFragmentToNoteFragment -> MainFragmentDirections.actionMainFragmentToAddNoteFragment(
        category
    )

    is Direction.NoteFragmentToRecorderFragment -> NoteFragmentDirections.actionNoteFragmentToRecorderFragment(
        fileMediaName
    )
}