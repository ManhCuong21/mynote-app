package com.example.mynote.core.navigation

import android.app.Activity
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.example.mynote.R
import com.example.mynote.core.external.checkMainThread
import javax.inject.Inject
import com.example.mynote.core.navigation.MainNavigator.Direction
import com.example.mynote.core.external.safeNavigate
import com.example.mynote.ui.main.MainFragmentDirections
import com.example.mynote.ui.main.home.HomeFragmentDirections

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
    is Direction.MainFragmentToAddNoteFragment -> MainFragmentDirections.actionMainFragmentToAddNoteFragment(
        idCategoryNote
    )
}