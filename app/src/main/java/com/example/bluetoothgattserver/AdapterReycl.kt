package com.example.bluetoothgattserver

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothgattserver.databinding.DiscoveredDedviceItemBinding
@SuppressLint("MissingPermission")
class ConnectedDevicesAdapter(
) : RecyclerView.Adapter<DeviceViewHolder>() {
    private var items = mutableListOf<Pair<String, BluetoothDevice>>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = DiscoveredDedviceItemBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: MutableList<Pair<String, BluetoothDevice>>) {
        items = newItems
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: MutableList<Pair<String, BluetoothDevice>>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
class DeviceViewHolder(private val binding: DiscoveredDedviceItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Pair<String, BluetoothDevice>) {
       binding.apply{
           textViewNameFound.text = item.first
           textViewMacFound.text = item.second.address
       }


    }
}

