package com.example.presentation.category

import android.content.Context
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.loadImage
import com.example.core.core.model.ItemCategory
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.ItemListAddCategoryBinding

class CategoryAdapter(
    defaultPosition: Int,
    private val isDarkMode: Boolean,
    private val onItemClicked: (ItemCategory) -> Unit
) : ListAdapter<ItemCategory, CategoryAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ItemCategory>() {
        override fun areItemsTheSame(oldItem: ItemCategory, newItem: ItemCategory): Boolean =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: ItemCategory, newItem: ItemCategory): Boolean =
            oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)

    private var selectedPosition = defaultPosition
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        val background = if (isDarkMode) ContextCompat.getColor(
            holder.context,
            R.color.background_content_root_night
        ) else ContextCompat.getColor(holder.context, R.color.white)
        if (selectedPosition == holder.bindingAdapterPosition) {
            holder.binding.root.setBackgroundResource(R.drawable.border_item_category)
        } else {
            holder.binding.root.setBackgroundColor(background)
        }
    }

    inner class ViewHolder(
        val binding: ItemListAddCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
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
