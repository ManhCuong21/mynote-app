package com.example.presentation.note.adapter

import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.external.loadImageDrawable
import com.example.core.core.model.ItemRecord
import com.example.core.core.model.StatusRecord
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.R
import com.example.presentation.canvas.Timer
import com.example.presentation.databinding.ItemListRecordEditNoteBinding


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
        private lateinit var timer: Timer

        //        private var player: MediaPlayer? = null
        private var player: ExoPlayer? = null
        private var statusRecord = StatusRecord.CREATE
        fun bind(item: ItemRecord) = binding.apply {
            timer = Timer { time ->
                tvTimerRecord.text = time
                item.amplitudes.forEach {
                    audioWave.addAmplitude(it)
                }
            }
            btnPlayRecord.setOnClickListener {
                statusRecord = when (statusRecord) {
                    StatusRecord.CREATE -> StatusRecord.START
                    StatusRecord.START -> StatusRecord.PAUSE
                    StatusRecord.PAUSE -> StatusRecord.RESUME
                    StatusRecord.RESUME -> StatusRecord.PAUSE
                }
                onPlayRecord(statusRecord, item)
            }
            btnDeleteRecord.setOnClickListener {
                bindingAdapterPosition.let {
                    if (it != RecyclerView.NO_POSITION) {
                        onItemDelete(item.pathDirectory)
                    }
                }
            }
        }

        private fun onPlayRecord(status: StatusRecord, item: ItemRecord) {
            setUiPlaying(status)
            when (status) {
                StatusRecord.START -> startPlaying(item)
                StatusRecord.PAUSE -> pausePlaying()
                StatusRecord.RESUME -> player?.play()
                else -> stopPlaying()
            }
        }

        private fun setUiPlaying(status: StatusRecord) = binding.apply {
            if (status == StatusRecord.PAUSE) {
                btnPlayRecord.loadImageDrawable(R.drawable.icon_play_record)
            } else {
                btnPlayRecord.loadImageDrawable(R.drawable.icon_pause_play_record)
            }
        }

        private fun playerListener() = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    stopPlaying()
                    binding.btnPlayRecord.loadImageDrawable(R.drawable.icon_play_record)
                }
            }
        }

        private fun startPlaying(item: ItemRecord) = binding.apply {
            player = ExoPlayer.Builder(root.context).build().also { exoPlayer ->
                val mediaItem = MediaItem.fromUri(item.pathRecord)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.addListener(playerListener())
                exoPlayer.play()
            }
            timer.start()
        }

        private fun pausePlaying() {
            player?.pause()
            timer.pause()
        }

        private fun stopPlaying() {
            player?.removeListener(playerListener())
            player?.release()
            player = null
            timer.stop()
            statusRecord = StatusRecord.CREATE
        }
    }
}