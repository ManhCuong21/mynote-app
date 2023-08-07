package com.example.presentation.main.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.loadImage
import com.example.core.core.model.CategoryUIModel
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.ItemListCategoryHomeBinding

class HomeListCategoryAdapter(
    private val isDarkMode: Boolean,
    private val onItemClicked: (Int) -> Unit
) : ListAdapter<CategoryUIModel, HomeListCategoryAdapter.ViewHolder>(
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

    private var selectedPosition = 0
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val backgroundCard = if (isDarkMode)
            ContextCompat.getColor(holder.context, R.color.background_content_root_night) else
            ContextCompat.getColor(holder.context, R.color.white)
        val backgroundCardSelected = if (isDarkMode) Color.parseColor("#4E4E4E") else
            Color.parseColor("#FFEAEA")
        if (selectedPosition == position) {
            holder.binding.root.setCardBackgroundColor(backgroundCardSelected)
        } else {
            holder.binding.root.setCardBackgroundColor(backgroundCard)
        }
        holder.bind(getItem(position))
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setCurrentTab(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemListCategoryHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val context: Context = binding.root.context
        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: CategoryUIModel) = binding.apply {
            tvTitleCategory.text = item.titleCategory
            item.imageCategory.let { imgItemCategory.loadImage(it) }
            root.setOnClickListener {
                bindingAdapterPosition.let {
                    if (it != RecyclerView.NO_POSITION) {
                        selectedPosition = it
                        notifyDataSetChanged()
                        onItemClicked(it)
                    }
                }
            }
        }
    }
}