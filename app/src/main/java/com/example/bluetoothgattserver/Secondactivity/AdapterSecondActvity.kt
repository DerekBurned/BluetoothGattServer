/*
package com.example.bluetoothgattserver.Secondactivity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothgattserver.connectedDevices
import com.example.bluetoothgattserver.connectedDevices.DeviceViewHolder
import com.example.bluetoothgattserver.databinding.DiscoveredDedviceItemBinding

class AdapterSecondActvity(
    private val onDeviceCheck: (BluetoothDevice, Boolean) -> Unit)
    : ListAdapter<BluetoothDevice, DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): connectedDevices.DeviceViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        TODO("Not yet implemented")
    }


    inner class DeviceViewHolder(private val binding: DiscoveredDedviceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingPermission")
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


    class DeviceDiffCallBack : DiffUtil.ItemCallback<BluetoothDevice>(){
        override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(
            oldItem: BluetoothDevice,
            newItem: BluetoothDevice
        ): Boolean {
            return oldItem == newItem        }

    }


}*/
