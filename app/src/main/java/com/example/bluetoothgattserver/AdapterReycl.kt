package com.example.bluetoothgattserver

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothgattserver.databinding.DiscoveredDedviceItemBinding
@SuppressLint("MissingPermission")
class connectedDevices(
    private val onDeviceCheck: (Pair<String,BluetoothDevice>, Boolean) -> Unit)
    : ListAdapter<Pair<String,BluetoothDevice>, connectedDevices.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding  = DiscoveredDedviceItemBinding.
            inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = getItem(position)
        holder.bind(device, onDeviceCheck)
    }

    inner class DeviceViewHolder(private val binding: DiscoveredDedviceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(device: Pair<String,BluetoothDevice>, onDeviceCheck: (Pair<String,BluetoothDevice>, Boolean) -> Unit) {
            binding.textViewNameFound.text  = device.first
            binding.textViewMacFound.text = device.second.address
            binding.checkBoxFound.setOnCheckedChangeListener(null)
            binding.checkBoxFound.isChecked = false

            binding.checkBoxFound.setOnCheckedChangeListener { _, isChecked ->
                onDeviceCheck(device, isChecked)
            }

        }
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<Pair<String,BluetoothDevice>>() {
        override fun areItemsTheSame(
            oldItem: Pair<String,BluetoothDevice>, newItem: Pair<String,BluetoothDevice>
        ): Boolean {
            return oldItem.first == newItem.first
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: Pair<String,BluetoothDevice>, newItem: Pair<String,BluetoothDevice>
        ): Boolean {
            return oldItem == newItem
        }
    }
}

