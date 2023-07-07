package com.example.mynote.ui.main.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynote.core.external.loadImage
import com.example.mynote.core.viewbinding.inflateViewBinding
import com.example.mynote.databinding.ItemListCategoryHomeBinding
import com.example.mynote.domain.model.CategoryModel

class HomeListCategoryAdapter(
    private val onItemClicked: (CategoryModel) -> Unit
) : ListAdapter<CategoryModel, HomeListCategoryAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<CategoryModel>() {
        override fun areItemsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean =
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
        fun bind(item: CategoryModel) = binding.apply {
            tvTitleCategory.text = item.title
            item.image?.let { imgItemCategory.loadImage(it) }
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