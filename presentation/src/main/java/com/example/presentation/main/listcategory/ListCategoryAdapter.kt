package com.example.presentation.main.listcategory

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.loadImage
import com.example.core.core.model.CategoryUIModel
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.databinding.ItemListCategoryBinding

class ListCategoryAdapter : ListAdapter<CategoryUIModel, ListCategoryAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<CategoryUIModel>() {
        override fun areItemsTheSame(oldItem: CategoryUIModel, newItem: CategoryUIModel): Boolean =
            oldItem.idCategory == newItem.idCategory

        override fun areContentsTheSame(
            oldItem: CategoryUIModel,
            newItem: CategoryUIModel
        ): Boolean =
            oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class ViewHolder(val binding: ItemListCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryUIModel) = binding.apply {
            imgCategory.loadImage(item.imageCategory)
            tvTitleCategory.text = item.titleCategory
        }
    }
}