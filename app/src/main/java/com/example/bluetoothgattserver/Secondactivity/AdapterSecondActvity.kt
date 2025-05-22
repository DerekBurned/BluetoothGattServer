package com.example.bluetoothgattserver.Secondactivity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothgattserver.connectedDevices.DeviceViewHolder
import com.example.bluetoothgattserver.databinding.ActivitySendItemBinding

////Ciśniomierz 3 (Sys.(ciśnienie skurczowe)   dia.(Ciśnienie rozkurczowe) pul.(tętno) (int) )
//, termometr 1 (temp (double)), glukometr 1 ((double) stęrzenie glukosy we krwi (mg/dL)/(mmol/L) )
//, pulsoksymetr 2((double)% saturacja tlenu(SpO2) (int)tętno (pul.))
class AdapterSecondActvity(
    private val onDeviceCheck: (BluetoothDevice, Boolean) -> Unit)
    : ListAdapter<BluetoothDevice, AdapterSecondActvity.DeviceViewHolderSecondActivity>(DeviceDiffCallBackSecondActivity()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeviceViewHolderSecondActivity {
        val binding = ActivitySendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolderSecondActivity(binding)
    }


    override fun onBindViewHolder(holder: DeviceViewHolderSecondActivity, position: Int) {
    }




    inner class DeviceViewHolderSecondActivity(private val binding: ActivitySendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingPermission")
        fun bind(device: BluetoothDevice, onDeviceCheck: (BluetoothDevice, Boolean) -> Unit) {


        }
    }


    class DeviceDiffCallBackSecondActivity : DiffUtil.ItemCallback<BluetoothDevice>(){
        override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(
            oldItem: BluetoothDevice,
            newItem: BluetoothDevice
        ): Boolean {
            return oldItem == newItem        }

    }


}
