package com.example.mynote.ui.dialog.list

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynote.core.external.loadImage
import com.example.mynote.core.viewbinding.inflateViewBinding
import com.example.mynote.databinding.ItemListDialogBinding

class ListDialogAdapter(
    private val onItemClicked: (Int) -> Unit
) : ListAdapter<ListDialogItem, ListDialogAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ListDialogItem>() {
        override fun areItemsTheSame(oldItem: ListDialogItem, newItem: ListDialogItem): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: ListDialogItem, newItem: ListDialogItem): Boolean =
            true
    }
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListDialogAdapter.ViewHolder = ViewHolder(parent inflateViewBinding false)

    override fun onBindViewHolder(holder: ListDialogAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private var selectedPosition = 0

    inner class ViewHolder(
        val binding: ItemListDialogBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListDialogItem) = binding.apply {
            imgItem.loadImage(item.image)
            tvTitleItem.text = item.title
            cbItem.isVisible = item.isVisibleCheckBox
            cbItem.isChecked = selectedPosition == bindingAdapterPosition
            vLineBottom.isVisible = bindingAdapterPosition != currentList.size - 1
            root.setOnClickListener {
                bindingAdapterPosition.let {
                    if (it != RecyclerView.NO_POSITION) {
                        notifyItemChanged(selectedPosition)
                        selectedPosition = it
                        notifyItemChanged(selectedPosition)
                        onItemClicked(bindingAdapterPosition)
                    }
                }
            }
        }
    }
}