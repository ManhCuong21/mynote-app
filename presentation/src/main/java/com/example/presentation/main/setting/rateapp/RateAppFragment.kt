package com.example.presentation.main.setting.rateapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.FragmentRateAppBinding
import com.example.presentation.dialog.text.showTextDialog

fun Fragment.showRateAppDialog(
    tag: String = this::class.java.simpleName,
) {
    RateAppFragment.getInstance()
        .show(
            requireActivity().supportFragmentManager,
            "${RateAppFragment.RATE_APP_DIALOG_FRAGMENT_TAG}.$tag"
        )
}

class RateAppFragment : DialogFragment() {
    private lateinit var binding: FragmentRateAppBinding
    private var starRate = 0F

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_AlertDialogFullScreenTransparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = layoutInflater.inflateViewBinding(
            parent = container,
            attachToParent = false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeRating()
        setupClickListener()
    }

    private fun setupClickListener() = binding.apply {
        btnRateApp.setOnClickListener {
            if (starRate > 3) {
                rateApp()
                dismiss()
            } else {
                showTextDialog {
                    textContent("Thank you for feedback. We will try to improve your experience")
                    negativeButtonAction("OK") {
                        dismiss()
                    }
                }
            }
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun changeRating() = binding.apply {
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            if (rating <= 1) {
                imgRating.setImageResource(R.drawable.rate_one)
            } else if (rating <= 3) {
                imgRating.setImageResource(R.drawable.rate_three)
            } else {
                imgRating.setImageResource(R.drawable.rate_five)
            }
            animateImage(imgRating)
            starRate = rating
        }
    }

    private fun animateImage(ratingImage: ImageView) {
        val scaleAnimation = ScaleAnimation(
            0f,
            1f,
            0f,
            1f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        scaleAnimation.fillAfter = true
        scaleAnimation.duration = 200
        ratingImage.startAnimation(scaleAnimation)
    }

    private fun rateApp() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data =
            Uri.parse("https://play.google.com/store/apps/details?id=" + requireActivity().packageName)
        requireActivity().startActivity(intent)
    }

    companion object {
        fun getInstance(): RateAppFragment {
            return RateAppFragment()
        }

        const val RATE_APP_DIALOG_FRAGMENT_TAG = "RateAppFragment"
    }
}