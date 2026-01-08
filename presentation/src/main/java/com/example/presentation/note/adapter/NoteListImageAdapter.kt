package com.example.presentation.note.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.loadImageFile
import com.example.core.core.model.ItemImage
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.databinding.ItemListImageNoteBinding

class NoteListImageAdapter(
    private val onItemEdit: (String) -> Unit,
    private val onItemDelete: (String) -> Unit
) : ListAdapter<ItemImage, NoteListImageAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ItemImage>() {
        override fun areItemsTheSame(oldItem: ItemImage, newItem: ItemImage): Boolean =
            oldItem.imagePath == newItem.imagePath

        override fun areContentsTheSame(oldItem: ItemImage, newItem: ItemImage): Boolean =
            oldItem == newItem
    }
) {
    inner class ViewHolder(val binding: ItemListImageNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemImage) = binding.apply {
            imgItem.loadImageFile(item.imagePath)
            imgDelete.setOnClickListener {
                bindingAdapterPosition.let {
                    if (it != RecyclerView.NO_POSITION) {
                        onItemDelete(item.imagePath)
                    }
                }
            }
            root.setOnClickListener {
                onItemEdit(item.imagePath)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)
}