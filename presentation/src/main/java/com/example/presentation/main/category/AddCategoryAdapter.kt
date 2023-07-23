package com.example.presentation.main.category

import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.loadImage
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.ItemListAddCategoryBinding

class AddCategoryAdapter(
    private val onItemClicked: (ItemCategory) -> Unit
) : ListAdapter<ItemCategory, AddCategoryAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ItemCategory>() {
        override fun areItemsTheSame(oldItem: ItemCategory, newItem: ItemCategory): Boolean =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: ItemCategory, newItem: ItemCategory): Boolean =
            oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)

    private var selectedPosition = 0
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        if (selectedPosition == holder.bindingAdapterPosition) {
            holder.binding.root.setBackgroundResource(R.drawable.border_item_category)
        } else {
            holder.binding.root.setBackgroundColor(Color.WHITE)
        }
    }

    inner class ViewHolder(
        val binding: ItemListAddCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemCategory) = binding.apply {
            imgItemCategory.loadImage(item.image)
            root.setOnClickListener {
                bindingAdapterPosition.let {
                    if (it != RecyclerView.NO_POSITION) {
                        notifyItemChanged(selectedPosition)
                        selectedPosition = it
                        notifyItemChanged(selectedPosition)
                        onItemClicked(getItem(it))
                    }
                }
            }
        }
    }
}
