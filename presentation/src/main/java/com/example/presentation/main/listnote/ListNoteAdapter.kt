package com.example.presentation.main.listnote

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.AppConstants.DATE_FORMAT_TIME_ALL
import com.example.core.core.model.NoteUIModel
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.databinding.ItemListNoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListNoteAdapter : ListAdapter<NoteUIModel, ListNoteAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<NoteUIModel>() {
        override fun areItemsTheSame(oldItem: NoteUIModel, newItem: NoteUIModel): Boolean =
            oldItem.idNote == newItem.idNote

        override fun areContentsTheSame(oldItem: NoteUIModel, newItem: NoteUIModel): Boolean =
            oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemListNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context
        fun bind(item: NoteUIModel) = binding.apply {
            imgCategoryNote.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    item.categoryNote.imageCategory
                )
            )
            if (bindingAdapterPosition == 0) {
                vLineTop.visibility = View.INVISIBLE
            }
            if (bindingAdapterPosition == (bindingAdapter?.itemCount ?: 1) - 1) {
                vLineBottom.visibility = View.INVISIBLE
            }
            vColorNote.setBackgroundColor(Color.parseColor(item.colorTitleNote))
            tvTitleNote.text = item.titleNote
            tvContentNote.text = item.contentNote
            tvTimeNote.text = formatDate(item.timeNote)
        }

        private fun formatDate(time: Long): String {
            val simpleDateFormat = SimpleDateFormat(DATE_FORMAT_TIME_ALL, Locale.getDefault())
            return simpleDateFormat.format(Date(time))
        }
    }
}