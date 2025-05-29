package com.example.bluetoothgattserver.Secondactivity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothgattserver.R
import com.example.bluetoothgattserver.databinding.ActivitySendItemBinding
import androidx.core.view.isVisible



class AdapterSecondActvity(
    private val onDeviceCheck: (BluetoothDevice, String, Boolean) -> Unit
) : RecyclerView.Adapter<AdapterSecondActvity.DeviceViewHolderSecondActivity>() {

    private val deviceDataList = mutableListOf<DeviceData>()
    private var previousConnectedDevices = emptyList<Pair<String, BluetoothDevice>>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<DeviceData>) {
        deviceDataList.clear()
        deviceDataList.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateConnectedDevices(currentDevices: List<Pair<String, BluetoothDevice>>) {
        // Find disconnected devices
        val disconnectedDevices = previousConnectedDevices.filter { prevDevice ->
            currentDevices.none { it.second.address == prevDevice.second.address }
        }

        // Remove disconnected devices from deviceDataList
        disconnectedDevices.forEach { disconnected ->
            deviceDataList.removeAll { it.deviceId == disconnected.second.address }
        }

        // Add new devices
        currentDevices.forEach { (name, device) ->
            if (deviceDataList.none { it.deviceId == device.address }) {
                deviceDataList.add(DeviceData(name, device))
                Log.d("DeviceConnection", "New device added: ${device.address}")
            }
        }

        previousConnectedDevices = currentDevices
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
            onDeviceCheck: (BluetoothDevice, String, Boolean) -> Unit
        ) {
            binding.textViewItem.text = deviceData.name.ifEmpty { "Unknown" }

            // Expand/collapse params layout on textViewItem click
            binding.textViewItem.setOnClickListener {
                if (binding.linearLayoutDeviceParam.isVisible) {
                    collapseParamsLayout()
                } else {
                    expandParamsLayout(deviceData, onDeviceCheck)
                }
            }

            // Reset checkbox listener before setting checked state to avoid unwanted triggers
            binding.checkboxDevice.setOnCheckedChangeListener(null)
            binding.checkboxDevice.isChecked = false
            binding.checkboxDevice.setOnCheckedChangeListener { _, isChecked ->
                // Send callback with current combined input string and checkbox state
                val combinedInputs = getCombinedInputs()
                onDeviceCheck(deviceData.device, combinedInputs, isChecked)
            }
        }

        private fun expandParamsLayout(
            deviceData: DeviceData,
            onDeviceCheck: (BluetoothDevice, String, Boolean) -> Unit
        ) {
            binding.linearLayoutDeviceParam.visibility = View.VISIBLE
            binding.linearLayoutDeviceParam.removeAllViews()

            when (deviceData.name) {
                "Ciśniomierz" -> {
                    addNumberEditText("Sys. Ciśnienie skurczowe (mmHg)", 0, 50.0..250.0, deviceData, onDeviceCheck)
                    addNumberEditText("Dia. Ciśnienie rozkurczowe (mmHg)", 1, 30.0..150.0, deviceData, onDeviceCheck)
                    addNumberEditText("Tętno (bpm)", 2, 40.0..200.0, deviceData, onDeviceCheck)
                }
                "Termometr" -> {
                    addNumberEditText("Temperatura (°C)", 0, 35.0..42.0, deviceData, onDeviceCheck)
                }
                "Glukometr" -> {
                    addNumberEditText("Stężenie glukozy", 0, 1.0..30.0, deviceData, onDeviceCheck)
                    addSpinner(deviceData, listOf("mg/dL", "mmol/L"), 1, onDeviceCheck)
                }
                "Pulsoksymetr" -> {
                    addNumberEditText("Saturacja tlenu (SpO2 %)", 0, 70.0..100.0, deviceData, onDeviceCheck)
                    addNumberEditText("Tętno (bpm)", 1, 40.0..200.0, deviceData, onDeviceCheck)
                }
                else -> {
                    addNumberEditText("Wartość", 0, null, deviceData, onDeviceCheck)
                }
            }

            binding.linearLayoutDeviceParam.startAnimation(
                AnimationUtils.loadAnimation(binding.root.context, R.anim.fade_slide_in)
            )
        }

        private fun collapseParamsLayout() {
            val fadeOut = AnimationUtils.loadAnimation(binding.root.context, R.anim.fade_out)
            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    binding.linearLayoutDeviceParam.visibility = View.GONE
                    binding.linearLayoutDeviceParam.removeAllViews()
                }
            })
            binding.linearLayoutDeviceParam.startAnimation(fadeOut)
        }

        private fun addNumberEditText(
            hint: String,
            index: Int,
            validRange: ClosedRange<Double>?,
            deviceData: DeviceData,
            onDeviceCheck: (BluetoothDevice, String, Boolean) -> Unit
        ) {
            val editText = EditText(binding.root.context).apply {
                this.hint = hint
                textSize = 16f
                setPadding(16, 8, 16, 8)
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 8.dpToPx(context)
                }


                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        val input = s?.toString()?.trim() ?: ""

                        if (index < deviceData.values.size) {
                            deviceData.values[index] = input
                        } else {
                            deviceData.values.add(input)
                        }

                        error = when {
                            input.isEmpty() -> "Nie może być puste"
                            !isValidNumber(input) -> "Musi być liczbą"
                            validRange != null && !isInRange(input, validRange) ->
                                "Dopuszczalny zakres: ${validRange.start} - ${validRange.endInclusive}"
                            else -> null
                        }

                        // Call callback with combined string on every input change, passing current checkbox state
                        val combinedInputs = getCombinedInputs()
                        onDeviceCheck(deviceData.device, combinedInputs, binding.checkboxDevice.isChecked)
                    }
                })
            }

            animateView(editText, index)
            binding.linearLayoutDeviceParam.addView(editText)
        }

        private fun addSpinner(
            deviceData: DeviceData,
            items: List<String>,
            index: Int,
            onDeviceCheck: (BluetoothDevice, String, Boolean) -> Unit
        ) {
            val spinner = Spinner(binding.root.context).apply {
                adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 8.dpToPx(context)
                }

                // Set spinner selection from stored value or default to first
                setSelection(items.indexOf(deviceData.values.getOrNull(index) ?: items.first()))

                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (index < deviceData.values.size) {
                            deviceData.values[index] = items[position]
                        } else {
                            deviceData.values.add(items[position])
                        }

                        // Call callback with combined string on selection change, passing current checkbox state
                        val combinedInputs = getCombinedInputs()
                        onDeviceCheck(deviceData.device, combinedInputs, binding.checkboxDevice.isChecked)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            animateView(spinner, index)
            binding.linearLayoutDeviceParam.addView(spinner)
        }

        // Combine all input values (EditTexts and Spinners) into one comma-separated string
        private fun getCombinedInputs(): String {
            val inputs = mutableListOf<String>()

            // Iterate all views inside linearLayoutDeviceParam
            for (i in 0 until binding.linearLayoutDeviceParam.childCount) {
                val view = binding.linearLayoutDeviceParam.getChildAt(i)

                when (view) {
                    is EditText -> {
                        inputs.add(view.text.toString().trim())
                    }
                    is Spinner -> {
                        inputs.add(view.selectedItem.toString())
                    }
                }
            }

            // Combine with comma separator (or customize as needed)
            return inputs.joinToString(separator = ", ")
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