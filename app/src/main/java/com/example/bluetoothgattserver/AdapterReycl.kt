package com.example.bluetoothgattserver

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothgattserver.databinding.DiscoveredDedviceItemBinding

@SuppressLint("MissingPermission")
class ConnectedDevicesAdapter()

    : ListAdapter<BluetoothDomain, ConnectedDevicesAdapter.DeviceViewHolder>(
    DeviceDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding =
            DiscoveredDedviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = getItem(position)
        holder.bind(device)
    }

    inner class DeviceViewHolder(private val binding: DiscoveredDedviceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: BluetoothDomain) {
            binding.textViewNameFound.text = device.name
            binding.textViewMacFound.text = device.device.address
        }
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<BluetoothDomain>() {
        override fun areItemsTheSame(oldItem: BluetoothDomain, newItem: BluetoothDomain): Boolean {
            return oldItem.device.address == newItem.device.address
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: BluetoothDomain, newItem: BluetoothDomain): Boolean {
            return oldItem == newItem
        }
    }
}

