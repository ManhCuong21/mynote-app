package com.example.core.core.external

import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

fun ImageView.loadImage(
    pathImage: Any
) {
    Glide.with(context)
        .load(pathImage)
        .fitCenter()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun EditText.onDidEndEditing(action: () -> Unit) {
    setOnEditorActionListener { v, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_PREVIOUS) {
            action()
            v.clearFocus()
        }
        false
    }
    setOnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            action()
        }
    }
}