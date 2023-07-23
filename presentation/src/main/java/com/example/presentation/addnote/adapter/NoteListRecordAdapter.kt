package com.example.presentation.addnote.adapter

import android.media.MediaPlayer
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.model.ItemRecord
import com.example.core.core.model.StatusRecord
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.databinding.ItemListRecordEditNoteBinding
import java.io.IOException

class NoteListRecordAdapter(
    private val onItemDelete: (String) -> Unit
) : ListAdapter<ItemRecord, NoteListRecordAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<ItemRecord>() {
        override fun areItemsTheSame(oldItem: ItemRecord, newItem: ItemRecord): Boolean =
            oldItem.pathRecord == newItem.pathRecord

        override fun areContentsTheSame(oldItem: ItemRecord, newItem: ItemRecord): Boolean =
            oldItem == newItem
    }
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder =
        ViewHolder(parent inflateViewBinding false)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: ItemListRecordEditNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var player: MediaPlayer? = null
        private var statusRecord = StatusRecord.CREATE
        private val context = binding.root.context
        fun bind(item: ItemRecord) = binding.apply {
            btnPlayRecord.setOnClickListener {
                statusRecord = when (statusRecord) {
                    StatusRecord.CREATE -> StatusRecord.START
                    StatusRecord.START -> StatusRecord.PAUSE
                    StatusRecord.PAUSE -> StatusRecord.RESUME
                    StatusRecord.RESUME -> StatusRecord.PAUSE
                }
                onPlayRecord(statusRecord, item.pathRecord)
            }
            btnDeleteRecord.setOnClickListener {
                bindingAdapterPosition.let {
                    if (it != RecyclerView.NO_POSITION) {
                        onItemDelete(item.pathRecord)
                    }
                }
            }
        }

        private fun onPlayRecord(status: StatusRecord, fileName: String) {
            setUiPlaying(status)
            when (status) {
                StatusRecord.START -> startPlaying(fileName)
                StatusRecord.PAUSE -> pausePlaying()
                StatusRecord.RESUME -> player?.start()
                else -> stopPlaying()
            }
        }

        private fun setUiPlaying(status: StatusRecord) = binding.apply {
            if (status == StatusRecord.PAUSE) {
                btnPlayRecord.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.icon_play_record
                    )
                )
            } else {
                btnPlayRecord.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.icon_pause_play_record
                    )
                )
            }
            player?.setOnCompletionListener {
                binding.btnPlayRecord.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.icon_play_record
                    )
                )
                player?.stop()
            }
        }

        private fun startPlaying(fileName: String) {
            player = MediaPlayer().apply {
                try {
                    setDataSource(fileName)
                    prepare()
                    start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        private fun pausePlaying() {
            player?.pause()
        }

        private fun stopPlaying() {
            player?.release()
            player = null
        }

    }
}