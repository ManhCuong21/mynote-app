package com.example.mynote.ui.activity

import android.os.Bundle
import androidx.fragment.app.FragmentContainerView
import com.example.mynote.R
import com.example.mynote.base.BaseMainActivity
import com.example.mynote.core.sharepref.SharedPrefersManager
import com.example.mynote.core.viewbinding.viewBinding
import com.example.mynote.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : BaseMainActivity() {
    private val binding by viewBinding<ActivityMainBinding>()

    @Inject
    lateinit var sharedPrefersManager: SharedPrefersManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        if (sharedPrefersManager.darkModeTheme) {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//            sharedPrefersManager.darkModeTheme = false
//        } else {
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            sharedPrefersManager.darkModeTheme = true
//        }
    }

    override val navHostFragmentActivityMain: FragmentContainerView
        get() = binding.navHostFragmentActivityMain
}