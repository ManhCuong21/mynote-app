package com.example.core.core.external

import android.annotation.SuppressLint
import android.widget.ImageView
import coil3.imageLoader
import coil3.load
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import coil3.request.target
import coil3.size.Scale
import com.example.core.R
import java.io.File

@SuppressLint("DiscouragedApi")
fun ImageView.loadImageDrawable(imageValue: String?) {
    val imageResId = try {
        context.resources.getIdentifier(imageValue, "drawable", context.packageName).let {
            if (it == 0) androidx.appcompat.R.drawable.abc_btn_default_mtrl_shape else it
        }
    } catch (e: Exception) {
        androidx.appcompat.R.drawable.abc_btn_default_mtrl_shape
        e.printStackTrace()
    }

    // Sử dụng Coil để load
    this.load(imageResId) {
        crossfade(true)
    }
}

fun ImageView.loadImageDrawable(imageResId: Int?) {
    this.load(imageResId) {
        crossfade(true)
    }
}


fun ImageView.loadImageFile(
    data: String,
    scaleType: Scale = Scale.FILL,
    placeholderRes: Int = R.drawable.ic_placeholder
) {
    val context = this.context
    val imageRequest = ImageRequest.Builder(context)
        .data(File(data))
        .crossfade(true)
        .placeholder(placeholderRes)
        .scale(scaleType)
        .error(R.drawable.error_image)
        .target(this)
        .build()

    context.imageLoader.enqueue(imageRequest)
}