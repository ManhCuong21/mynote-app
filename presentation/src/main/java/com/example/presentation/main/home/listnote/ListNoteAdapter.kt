package com.example.presentation.main.home.listnote

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.ActionNote
import com.example.core.core.external.AppConstants.DATE_FORMAT_TIME_12_HOUR
import com.example.core.core.external.AppConstants.DATE_FORMAT_TIME_24_HOUR
import com.example.core.core.external.formatDate
import com.example.core.core.external.loadImageDrawable
import com.example.core.core.model.NoteModel
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.DialogSettingNoteBinding
import com.example.presentation.databinding.ItemListNoteBinding

class ListNoteAdapter(
    private val format24Hour: Boolean,
    private val onItemClicked: (ActionNote, NoteModel) -> Unit
) : ListAdapter<NoteModel, ListNoteAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<NoteModel>() {
        override fun areItemsTheSame(oldItem: NoteModel, newItem: NoteModel): Boolean =
            oldItem.idNote == newItem.idNote

        override fun areContentsTheSame(oldItem: NoteModel, newItem: NoteModel): Boolean =
            oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), format24Hour)
    }

    inner class ViewHolder(private val binding: ItemListNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val context = binding.root.context
        fun bind(item: NoteModel, format24Hour: Boolean) = binding.apply {
            imgCategoryNote.loadImageDrawable(item.categoryNote?.imageCategory)
            vColorNote.setBackgroundColor(Color.parseColor(item.colorTitleNote))
            tvTitleNote.text = item.titleNote
            tvContentNote.text = item.contentNote
            val dateFormat =
                if (!format24Hour) DATE_FORMAT_TIME_12_HOUR else DATE_FORMAT_TIME_24_HOUR
            tvTimeNote.text =
                context.getString(R.string.format_date_note, formatDate(dateFormat, item.timeNote))
            vHaveImage.isVisible = item.hasImage
            vHaveRecord.isVisible = item.hasRecord
            vHaveNotification.isVisible = item.notificationModel?.idNotification != null
            root.setOnClickListener {
                val binding = DialogSettingNoteBinding.inflate(LayoutInflater.from(context))
                val builder = AlertDialog.Builder(context)
                builder.setView(binding.root)
                val dialog = builder.create()
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()
                binding.apply {
                    btnShowOnMap.setOnClickListener {
                        onItemClicked(ActionNote.NOTIFICATION, item)
                        dialog.dismiss()
                    }
                    btnEditNote.setOnClickListener {
                        onItemClicked(ActionNote.UPDATE_NOTE, item)
                        dialog.dismiss()
                    }
                    btnChangeCategory.setOnClickListener {
                        onItemClicked(ActionNote.CHANGE_CATEGORY, item)
                        dialog.dismiss()
                    }
                    btnDeleteNote.setOnClickListener {
                        onItemClicked(ActionNote.DELETE_NOTE, item)
                        dialog.dismiss()
                    }
                    btnCancel.setOnClickListener {
                        dialog.dismiss()
                    }
                }
            }
        }
    }
}