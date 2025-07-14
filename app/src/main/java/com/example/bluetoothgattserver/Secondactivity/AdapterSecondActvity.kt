package com.example.bluetoothgattserver.Secondactivity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothgattserver.R
import com.example.bluetoothgattserver.databinding.ActivitySendItemBinding

class AdapterSecondActvity( val context: Context,
    private val onDeviceCheck: (BluetoothDevice?, String, Boolean) -> Unit
) : RecyclerView.Adapter<AdapterSecondActvity.DeviceViewHolderSecondActivity>() {

    private val deviceTypes = listOf("Ciśniomierz", "Termometr", "Glukometr", "Pulsoksymetr")
    private val connectedDevices = mutableMapOf<String, BluetoothDevice>()
    private val inputValuesMap = mutableMapOf<String, MutableList<String>>()

    private val expandedStates = mutableMapOf<String, Boolean>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateConnectedDevices(devices: List<Pair<String, BluetoothDevice>>) {
        connectedDevices.clear()
        connectedDevices.putAll(devices)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = deviceTypes.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolderSecondActivity {
        val binding = ActivitySendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolderSecondActivity(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolderSecondActivity, position: Int) {
        val deviceName = deviceTypes[position]
        holder.bind(deviceName, connectedDevices[deviceName], onDeviceCheck)
    }

    inner class DeviceViewHolderSecondActivity(private val binding: ActivitySendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingPermission", "ResourceAsColor")
        fun bind(
            deviceName: String,
            device: BluetoothDevice?,
            onDeviceCheck: (BluetoothDevice, String, Boolean) -> Unit
        ) {
            binding.linearLayoutDeviceParam.removeAllViews()
            binding.textViewItem.text = deviceName

            val isConnected = device != null

            when (deviceName) {
                "Ciśnieniomierz" -> {
                    addNumberEditText(deviceName,
                        context.getString(R.string.sys_ci_nienie_skurczowe_mmhg), 0, 50.0..250.0, device, onDeviceCheck)
                    addNumberEditText(deviceName,
                        context.getString(R.string.dia_ci_nienie_rozkurczowe_mmhg), 1, 30.0..150.0, device, onDeviceCheck)
                    addNumberEditText(deviceName,
                        context.getString(R.string.t_tno_bpm), 2, 40.0..200.0, device, onDeviceCheck)
                }
                "Termometr" -> {
                    addNumberEditText(deviceName,
                        context.getString(R.string.temperatura_c), 0, 35.0..50.0, device, onDeviceCheck)
                }
                "Glukometr" -> {
                    addNumberEditText(deviceName,
                        context.getString(R.string.st_enie_glukozy), 0, 1.0..30.0, device, onDeviceCheck)
                    addUnitSpinner(deviceName, 1)
                }
                "Pulsoksymetr" -> {
                    addNumberEditText(deviceName,
                        context.getString(R.string.saturacja_tlenu_spo2), 0, 70.0..100.0, device, onDeviceCheck)
                    addNumberEditText(deviceName,
                        context.getString(R.string.t_tno_bpm), 1, 40.0..200.0, device, onDeviceCheck)

                }
                else -> {
                    addNumberEditText(deviceName, "Wartość", 0, null, device, onDeviceCheck)
                }
            }

            if (isConnected) {
                binding.textViewItem.setBackgroundResource(R.drawable.device_item)
                binding.checkboxDevice.isEnabled = true
            } else {
                binding.textViewItem.setBackgroundResource(R.drawable.device_item_disconnect)
                binding.checkboxDevice.isEnabled = false
                binding.checkboxDevice.isChecked = false
            }

            val isExpanded = expandedStates[deviceName] ?: false
            binding.linearLayoutDeviceParam.visibility = if (isExpanded) View.VISIBLE else View.GONE

            binding.textViewItem.setOnClickListener {
                val newExpanded = !binding.linearLayoutDeviceParam.isVisible
                binding.linearLayoutDeviceParam.visibility = if (newExpanded) View.VISIBLE else View.GONE
                expandedStates[deviceName] = newExpanded
            }

            binding.checkboxDevice.setOnCheckedChangeListener(null)
            binding.checkboxDevice.isChecked = false
            binding.checkboxDevice.setOnCheckedChangeListener { _, isChecked ->
                if (device != null) {
                    val combinedInputs = getCombinedInputs()
                    onDeviceCheck(device, combinedInputs, isChecked)
                }
            }
        }

        private fun addNumberEditText(
            deviceName: String,
            hint: String,
            index: Int,
            validRange: ClosedRange<Double>?,
            device: BluetoothDevice?,
            onDeviceCheck: (BluetoothDevice, String, Boolean) -> Unit
        ) {
            val editText = EditText(binding.root.context).apply {
                this.hint = hint
                textSize = 16f
                setPadding(16, 8, 16, 8)
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 8.dpToPx(context)
                }

                // Restore previous input if exists
                val deviceInputs = inputValuesMap[deviceName]
                if (deviceInputs != null && index < deviceInputs.size) {
                    setText(deviceInputs[index])
                }

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        val input = s?.toString()?.trim() ?: ""
                        val values = inputValuesMap.getOrPut(deviceName) { MutableList(10) { "" } }
                        if (index >= values.size) {
                            while (values.size <= index) values.add("")
                        }
                        values[index] = input

                        error = when {
                            input.isEmpty() -> "Cannot be empty"
                            !isValidNumber(input) -> "Must be a number"
                            validRange != null && !isInRange(input, validRange) -> "Allowed: ${validRange.start} - ${validRange.endInclusive}"
                            else -> null
                        }

                        val combinedInputs = getCombinedInputs()
                        if (device != null) {
                            onDeviceCheck(device, combinedInputs, binding.checkboxDevice.isChecked)
                        }
                    }
                })
            }
            animateView(editText, index)
            binding.linearLayoutDeviceParam.addView(editText)
        }


        private fun addUnitSpinner( deviceName: String,index: Int): String {
            val context = binding.root.context
            val spinner = Spinner(context)
            val options = listOf("mg/dL", "mmol/L")
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, options)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            val values = inputValuesMap[deviceName]
            if (values != null && index < values.size) {
                val selected = values[index]
                val selectedIndex = options.indexOf(selected)
                if (selectedIndex >= 0) {
                    spinner.setSelection(selectedIndex)
                }
            }

            spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    val selectedValue = options[pos]
                    val valueList = inputValuesMap.getOrPut(deviceName) { MutableList(10) { "" } }
                    if (index >= valueList.size) {
                        while (valueList.size <= index) valueList.add("")
                    }
                    valueList[index] = selectedValue
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

            spinner.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 8.dpToPx(context)
            }

            animateView(spinner, index)
            binding.linearLayoutDeviceParam.addView(spinner)
            return options[spinner.selectedItemPosition]
        }


        private fun getCombinedInputs(): String {
            val inputs = mutableListOf<String>()
            inputs.add(binding.textViewItem.text.toString())
            for (i in 0 until binding.linearLayoutDeviceParam.childCount) {
                when (val view = binding.linearLayoutDeviceParam.getChildAt(i)) {
                    is EditText -> inputs.add(view.text.toString().trim())
                    is Spinner -> inputs.add(view.selectedItem.toString())
                }
            }
            return inputs.joinToString(", ")
        }

        private fun animateView(view: View, index: Int) {
            val animation = AnimationUtils.loadAnimation(binding.root.context, R.anim.fade_slide_in)
            animation.startOffset = (index * 80).toLong()
            view.startAnimation(animation)
        }

        private fun Int.dpToPx(context: Context): Int {
            return (this * context.resources.displayMetrics.density).toInt()
        }

        private fun isValidNumber(input: String): Boolean {
            return input.toDoubleOrNull() != null
        }

        private fun isInRange(input: String, range: ClosedRange<Double>): Boolean {
            return input.toDoubleOrNull()?.let { it in range } ?: false
        }
    }
}
