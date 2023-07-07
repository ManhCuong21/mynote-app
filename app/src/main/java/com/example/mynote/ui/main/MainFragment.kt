package com.example.mynote.ui.main

import android.os.Bundle
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.mynote.R
import com.example.mynote.base.BaseFragment
import com.example.mynote.core.navigation.MainNavigator
import com.example.mynote.core.sharepref.SharedPrefersManager
import com.example.mynote.core.viewbinding.viewBinding
import com.example.mynote.databinding.FragmentMainBinding
import com.example.mynote.ui.main.home.HomeFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : BaseFragment(R.layout.fragment_main) {
    override val binding by viewBinding<FragmentMainBinding>()
    override val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var mainNavigator: MainNavigator

//    private var homeUIBottomNavVisible: Boolean = true

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    private inline val navController: NavController
        get() = binding.fragmentContainerView.getFragment<NavHostFragment>().navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (sharedPrefersManager.darkModeTheme){
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//            sharedPrefersManager.darkModeTheme = false
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            sharedPrefersManager.darkModeTheme = true
//        }
    }

    override fun setupViews() {
        requireActivity().onBackPressedDispatcher.addCallback(
            owner = viewLifecycleOwner,
            enabled = true
        ) {}
        setupBottomNav()
    }

    override fun bindViewModel() {
        //No TODO here
    }

    private fun setupBottomNav() = binding.bottomNavView.apply {
//        isVisible = homeUIBottomNavVisible
        onItemSelected = {
            when (it) {
                0 -> navController.navigate(HomeFragmentDirections.actionGlobalToHomeFragment())
                1 -> navController.navigate(HomeFragmentDirections.actionGlobalToCategoryFragment())
                2 -> navController.navigate(HomeFragmentDirections.actionGlobalToHomeFragment())
                3 -> navController.navigate(HomeFragmentDirections.actionGlobalToHomeFragment())
            }
        }
//        binding.bottomNavView.setupWithNavController(navController)
//        setupWithNavController(navController)
//        setOnItemReselectedListener {
//            // Pop the back stack to the start destination of the current navController graph
//            navController.popToStartDestination(it)
//        }
    }
}
