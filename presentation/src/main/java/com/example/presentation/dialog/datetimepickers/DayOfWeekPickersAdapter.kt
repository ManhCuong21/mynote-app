package com.example.presentation.dialog.datetimepickers

import android.graphics.Color
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.ItemListDayOfWeekNotificationBinding
import java.util.Calendar

class DayOfWeekPickersAdapter(
    private val listDefault: List<Int>,
    private val onItemClicked: (Int) -> Unit
) : ListAdapter<Int, DayOfWeekPickersAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean =
            oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: ItemListDayOfWeekNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context
        fun bind(item: Int) = binding.apply {
            var selectedPosition: Boolean
            binding.apply {
                selectedPosition = if (listDefault.contains(item)) {
                    tvTitleDayOfWeek.setBackgroundResource(R.drawable.border_item_date_time_pickers)
                    true
                } else {
                    tvTitleDayOfWeek.setBackgroundColor(Color.TRANSPARENT)
                    false
                }
            }
            tvTitleDayOfWeek.text = when (item) {
                Calendar.MONDAY -> "M"
                Calendar.TUESDAY -> "T"
                Calendar.WEDNESDAY -> "W"
                Calendar.THURSDAY -> "T"
                Calendar.FRIDAY -> "F"
                Calendar.SATURDAY -> "S"
                Calendar.SUNDAY -> "S"
                else -> ""
            }
            if (item == Calendar.SUNDAY) {
                tvTitleDayOfWeek.setTextColor(ContextCompat.getColor(context, R.color.redTitle))
            }
            btnDayOfWeek.setOnClickListener {
                selectedPosition = if (selectedPosition) {
                    tvTitleDayOfWeek.setBackgroundColor(Color.TRANSPARENT)
                    false
                } else {
                    tvTitleDayOfWeek.setBackgroundResource(R.drawable.border_item_date_time_pickers)
                    true
                }
                bindingAdapterPosition.let {
                    if (it != RecyclerView.NO_POSITION) {
                        onItemClicked(getItem(it))
                    }
                }
            }
        }
    }
}