package com.example.presentation.dialog.list

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.loadImageDrawable
import com.example.core.core.model.ListDialogItem
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.databinding.ItemListDialogBinding

class ListDialogAdapter(
    position: Int,
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
    ): ViewHolder = ViewHolder(parent inflateViewBinding false)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private var selectedPosition = position

    inner class ViewHolder(
        val binding: ItemListDialogBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListDialogItem) = binding.apply {
            imgItem.loadImageDrawable(item.image)
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
            cbItem.setOnClickListener {
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