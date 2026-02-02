package com.example.presentation.main.home.listnote.components

import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.AppConstants
import com.example.core.core.external.formatDate
import com.example.core.core.external.loadImageDrawable
import com.example.core.core.model.NoteModel
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.ItemListNoteBinding

class ListNoteAdapter(
    private val format24Hour: Boolean,
    private val isBiometric: Boolean,
    private val onItemClicked: (NoteModel) -> Unit,
    private val onRequireAuth: (NoteModel) -> Unit,
    private val onRequireOtp: (NoteModel) -> Unit
) : ListAdapter<NoteModel, ListNoteAdapter.ViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<NoteModel>() {
            override fun areItemsTheSame(oldItem: NoteModel, newItem: NoteModel) =
                oldItem.idNote == newItem.idNote

            override fun areContentsTheSame(oldItem: NoteModel, newItem: NoteModel) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), format24Hour)
    }

    inner class ViewHolder(
        private val binding: ItemListNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NoteModel, format24Hour: Boolean) = with(binding) {
            val context = root.context

            imgCategoryNote.loadImageDrawable(item.categoryNote?.imageCategory)
            lContentNote.setBackgroundColor(item.colorTitleNote.toColorInt())
            lTitleNote.setBackgroundColor(item.colorContentNote.toColorInt())
            tvTitleNote.text = item.titleNote

            val security = item.security || item.categoryNote?.securityCategory ?: false
            lnContentNote.isVisible = !security
            imgSecurityNote.isVisible = security
            tvContentNote.text = item.contentNote

            val dateFormat =
                if (format24Hour) AppConstants.DATE_FORMAT_TIME_24_HOUR else AppConstants.DATE_FORMAT_TIME_12_HOUR

            tvTimeNote.text = context.getString(
                R.string.format_date_note,
                formatDate(dateFormat, item.timeNote)
            )

            vHaveImage.isVisible = item.hasImage
            vHaveRecord.isVisible = item.hasRecord
            vHaveNotification.isVisible = item.notificationModel?.idNotification != null

            root.setOnClickListener {
                when {
                    isBiometric && security -> onRequireAuth(item)
                    security -> onRequireOtp(item)
                    else -> onItemClicked(item)
                }
            }
        }
    }
}