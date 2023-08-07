package com.example.presentation.main.home.listnote

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.AppConstants.DATE_FORMAT_TIME_12_HOUR
import com.example.core.core.external.AppConstants.DATE_FORMAT_TIME_24_HOUR
import com.example.core.core.model.NoteUIModel
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.databinding.ItemListNoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListNoteAdapter(
    private val format24Hour: Boolean
) : ListAdapter<NoteUIModel, ListNoteAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<NoteUIModel>() {
        override fun areItemsTheSame(oldItem: NoteUIModel, newItem: NoteUIModel): Boolean = false

        override fun areContentsTheSame(oldItem: NoteUIModel, newItem: NoteUIModel): Boolean =
            oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), format24Hour)
    }

    class ViewHolder(private val binding: ItemListNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context
        fun bind(item: NoteUIModel, format24Hour: Boolean) = binding.apply {
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
            tvTimeNote.text = formatDate(item.timeNote, format24Hour)
        }

        private fun formatDate(time: Long, format24Hour: Boolean): String {
            val dateFormat =
                if (!format24Hour) DATE_FORMAT_TIME_12_HOUR else DATE_FORMAT_TIME_24_HOUR
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.getDefault())
            return simpleDateFormat.format(Date(time))
        }
    }
}