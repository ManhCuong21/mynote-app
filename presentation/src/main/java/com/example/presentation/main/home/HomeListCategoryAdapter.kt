package com.example.presentation.main.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.loadImage
import com.example.core.core.model.CategoryUIModel
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.databinding.ItemListCategoryHomeBinding

class HomeListCategoryAdapter(
    private val onItemClicked: (CategoryUIModel) -> Unit
) : ListAdapter<CategoryUIModel, HomeListCategoryAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<CategoryUIModel>() {
        override fun areItemsTheSame(oldItem: CategoryUIModel, newItem: CategoryUIModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CategoryUIModel, newItem: CategoryUIModel): Boolean =
            oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)

    private var selectedPosition = 0
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (selectedPosition == position) {
            holder.binding.root.setCardBackgroundColor(Color.parseColor("#FFEAEA"))
        } else {
            holder.binding.root.setCardBackgroundColor(Color.WHITE)
        }
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: ItemListCategoryHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: CategoryUIModel) = binding.apply {
            tvTitleCategory.text = item.title
            item.image.let { imgItemCategory.loadImage(it) }
            root.setOnClickListener {
                bindingAdapterPosition.let {
                    if (it != RecyclerView.NO_POSITION) {
                        selectedPosition = it
                        notifyDataSetChanged()
                        onItemClicked(getItem(it))
                    }
                }
            }
        }
    }
}