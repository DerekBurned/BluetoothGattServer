package com.example.bluetoothgattserver.Secondactivity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothgattserver.databinding.ActivitySendItemBinding
import androidx.core.view.isVisible


class AdapterSecondActvity(
    private val onDeviceCheck: (BluetoothDevice, List<String>, Boolean) -> Unit
) : RecyclerView.Adapter<AdapterSecondActvity.DeviceViewHolderSecondActivity>() {

    private val deviceDataList = mutableListOf<DeviceData>()

    fun submitList(newList: List<DeviceData>) {
        deviceDataList.clear()
        deviceDataList.addAll(newList)
        notifyDataSetChanged()
    }

    fun getCurrentInputData(): List<DeviceData> = deviceDataList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolderSecondActivity {
        val binding = ActivitySendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolderSecondActivity(binding)
    }

    override fun getItemCount(): Int = deviceDataList.size

    override fun onBindViewHolder(holder: DeviceViewHolderSecondActivity, position: Int) {
        holder.bind(deviceDataList[position], onDeviceCheck)
    }

    inner class DeviceViewHolderSecondActivity(private val binding: ActivitySendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingPermission")
        fun bind(
            deviceData: DeviceData,
            onDeviceCheck: (BluetoothDevice, List<String>, Boolean) -> Unit
        ) {
            binding.textViewItem.text = deviceData.name.ifEmpty { "Unknown" }

            binding.textViewItem.setOnClickListener {
                binding.linearLayoutDeviceParam.visibility =
                    if (binding.linearLayoutDeviceParam.isVisible) View.GONE else View.VISIBLE
            }

            binding.linearLayoutDeviceParam.removeAllViews()

            for ((index, value) in deviceData.values.withIndex()) {
                val editText = EditText(binding.root.context).apply {
                    hint = value
                    textSize = 16f
                    setPadding(16, 8, 16, 8)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )


                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            val input = s?.toString()?.trim() ?: ""

                            // Update the list with current input
                            deviceData.values[index] = input

                            // Validate: not empty and is a number (int or double)
                            if (input.isEmpty()) {
                                error = "Cannot be empty"
                            } else if (!isValidNumber(input)) {
                                error = "Must be a number"
                            } else {
                                error = null
                            }
                        }
                    })
                }
                binding.linearLayoutDeviceParam.addView(editText)
            }

            binding.checkboxDevice.setOnCheckedChangeListener(null)
            binding.checkboxDevice.isChecked = false
            binding.checkboxDevice.setOnCheckedChangeListener { _, isChecked ->
                onDeviceCheck(deviceData.device, deviceData.values, isChecked)
            }
        }

        private fun isValidNumber(input: String): Boolean {
            // Check if input is a valid int or double
            return input.toIntOrNull() != null || input.toDoubleOrNull() != null
        }
    }
}
