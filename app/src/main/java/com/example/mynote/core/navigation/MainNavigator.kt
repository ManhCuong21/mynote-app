package com.example.mynote.core.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController

@MainThread
interface MainNavigator {
    val navController: NavController

    sealed interface Direction {
        data class MainFragmentToAddNoteFragment(val idCategoryNote: Int) : Direction
    }

    @MainThread
    fun navigate(direction: Direction)

    @MainThread
    fun popBackStack()
}