package com.example.mynote.ui.addnote.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mynote.R
import com.example.mynote.core.viewbinding.inflateViewBinding
import com.example.mynote.databinding.ItemListChooseColorBinding
import com.example.mynote.ui.addnote.ItemChooseColor

class NoteChooseColorAdapter(
    private val onItemClicked: (Int) -> Unit
) : ListAdapter<ItemChooseColor, NoteChooseColorAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ItemChooseColor>() {
        override fun areItemsTheSame(oldItem: ItemChooseColor, newItem: ItemChooseColor): Boolean =
            oldItem.colorTitle == newItem.colorTitle

        override fun areContentsTheSame(
            oldItem: ItemChooseColor,
            newItem: ItemChooseColor
        ): Boolean = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(parent inflateViewBinding false)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private var selectedPosition = 0

    inner class ViewHolder(private val binding: ItemListChooseColorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(item: ItemChooseColor) = binding.apply {
            imgChooseColor.setBackgroundColor(binding.root.context.getColor(item.colorTitle))
            val imageDrawable = if (selectedPosition == bindingAdapterPosition) {
                binding.root.context.getDrawable(R.drawable.baseline_check_24)
            } else null
            imgChooseColor.setImageDrawable(imageDrawable)
            root.setOnClickListener {
                bindingAdapterPosition.let {
                    if (it != RecyclerView.NO_POSITION) {
                        notifyItemChanged(selectedPosition)
                        selectedPosition = it
                        notifyItemChanged(selectedPosition)
                        onItemClicked(it)
                    }
                }
            }
        }
    }
}