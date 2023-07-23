package com.example.mynote.activity

import android.os.Bundle
import androidx.fragment.app.FragmentContainerView
import com.example.core.core.viewbinding.viewBinding
import com.example.mynote.R
import com.example.mynote.databinding.ActivityMainBinding


class MainActivity : BaseMainActivity() {
    private val binding by viewBinding<ActivityMainBinding>()

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