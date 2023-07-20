package com.example.mynote.ui.addnote.adapter

import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.ViewGroup
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynote.core.viewbinding.inflateViewBinding
import com.example.mynote.databinding.ItemListImageEditNoteBinding
import com.example.mynote.domain.model.ItemImage
import java.io.IOException

class NoteListImageAdapter(
    private val onItemDelete: (String) -> Unit
) : ListAdapter<ItemImage, NoteListImageAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ItemImage>() {
        override fun areItemsTheSame(oldItem: ItemImage, newItem: ItemImage): Boolean =
            oldItem.pathImage == newItem.pathImage

        override fun areContentsTheSame(oldItem: ItemImage, newItem: ItemImage): Boolean =
            oldItem == newItem
    }
) {
    inner class ViewHolder(val binding: ItemListImageEditNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemImage) = binding.apply {
            imgItem.setImageBitmap(rotateImage(item.pathImage, item.image))
            imgDelete.setOnClickListener {
                bindingAdapterPosition.let {
                    if (it != RecyclerView.NO_POSITION) {
                        onItemDelete(item.pathImage)
                    }
                }
            }
        }

        private fun rotateImage(pathImage: String, bitmap: Bitmap): Bitmap {
            var exifInterface: ExifInterface? = null
            try {
                exifInterface = ExifInterface(pathImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val orientation = exifInterface?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90F)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180F)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270F)
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)
}