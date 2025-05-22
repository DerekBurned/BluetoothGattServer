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
    private val onDeviceCheck: (BluetoothDevice, Boolean) -> Unit)
    : ListAdapter<BluetoothDevice, connectedDevices.DeviceViewHolder>(DeviceDiffCallback()) {

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

        fun bind(device: BluetoothDevice, onDeviceCheck: (BluetoothDevice, Boolean) -> Unit) {
            binding.textViewNameFound.text  = device.name
            binding.textViewMacFound.text = device.address
            binding.checkBoxFound.setOnCheckedChangeListener(null)
            binding.checkBoxFound.isChecked = false

            binding.checkBoxFound.setOnCheckedChangeListener { _, isChecked ->
                onDeviceCheck(device, isChecked)
            }

        }
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<BluetoothDevice>() {
        override fun areItemsTheSame(
            oldItem: BluetoothDevice, newItem: BluetoothDevice
        ): Boolean {
            return oldItem.address == newItem.address
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: BluetoothDevice, newItem: BluetoothDevice
        ): Boolean {
            return oldItem == newItem
        }
    }
}

