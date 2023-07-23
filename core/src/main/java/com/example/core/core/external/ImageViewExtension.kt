package com.example.core.core.external

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

