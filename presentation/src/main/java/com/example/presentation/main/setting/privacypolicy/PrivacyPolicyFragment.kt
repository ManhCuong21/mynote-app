package com.example.presentation.main.setting.privacypolicy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView

class PrivacyPolicyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val webView = WebView(requireContext())

        // Cấu hình WebView nếu cần
        webView.settings.javaScriptEnabled = false
        webView.settings.allowFileAccess = true

        // Load file HTML từ assets
        webView.loadUrl(":///android_afilesset/privacy_policy.html")

        return webView
    }
}