package com.example.presentation.main.setting.syncdata.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.core.core.viewbinding.inflateViewBinding
import com.example.presentation.databinding.ItemDeviceSyncBinding

class DeviceAdapter(
    private val onConnectClicked: (String) -> Unit
) : ListAdapter<Pair<String, String>, DeviceAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<Pair<String, String>>() {
        override fun areItemsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean =
            oldItem.first == newItem.first

        override fun areContentsTheSame(oldItem: Pair<String, String>, newItem: Pair<String, String>): Boolean =
            oldItem == newItem
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent inflateViewBinding false)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemDeviceSyncBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<String, String>) = binding.apply {
            val (endpointId, deviceName) = item
            tvDeviceName.text = deviceName
            tvDeviceId.text = endpointId

            btnConnect.setOnClickListener {
                onConnectClicked(endpointId)
            }

            root.setOnClickListener {
                onConnectClicked(endpointId)
            }
        }
    }
}