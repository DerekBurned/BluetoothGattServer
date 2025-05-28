package com.example.bluetoothgattserver.Secondactivity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothgattserver.R
import com.example.bluetoothgattserver.databinding.ActivitySendItemBinding
import androidx.core.view.isVisible


class AdapterSecondActvity(
    private val onDeviceCheck: (BluetoothDevice, List<String>, Boolean) -> Unit
) : RecyclerView.Adapter<AdapterSecondActvity.DeviceViewHolderSecondActivity>() {

    private val deviceDataList = mutableListOf<DeviceData>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<DeviceData>) {
        deviceDataList.clear()
        deviceDataList.addAll(newList)
        notifyDataSetChanged()
    }

    fun getCurrentInputData(): List<DeviceData> = deviceDataList

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeviceViewHolderSecondActivity {
        val binding =
            ActivitySendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                if (binding.linearLayoutDeviceParam.isVisible) {
                    collapseParamsLayout()
                } else {
                    expandParamsLayout(deviceData)
                }
            }

            binding.checkboxDevice.setOnCheckedChangeListener(null)
            binding.checkboxDevice.isChecked = false
            binding.checkboxDevice.setOnCheckedChangeListener { _, isChecked ->
                onDeviceCheck(deviceData.device, deviceData.values, isChecked)
            }
        }

        private fun expandParamsLayout(deviceData: DeviceData) {
            binding.linearLayoutDeviceParam.visibility = View.VISIBLE
            binding.linearLayoutDeviceParam.removeAllViews()

////Ciśniomierz 3 (Sys.(ciśnienie skurczowe)   dia.(Ciśnienie rozkurczowe) pul.(tętno) (int) )
//, termometr 1 (temp (double)), glukometr 1 ((double) stęrzenie glukosy we krwi (mg/dL)/(mmol/L) )
//, pulsoksymetr 2((double)% saturacja tlenu(SpO2) (int)tętno (pul.))

            when (deviceData.name) {
                "Ciśniomierz" -> {
                    addNumberEditText("Sys. Ciśnienie skurczowe", deviceData, 0)
                    addNumberEditText("dia. Ciśnienie rozkurczowe", deviceData, 1)
                    addNumberEditText("Tętno", deviceData, 2)
                }
                "Termometr" -> {
                    addNumberEditText("Temperatura C°", deviceData, 0)
                }
                "Glukometr" -> {
                    addNumberEditText("Stęrzenie glukosy", deviceData, 0)
                    addSpinner(deviceData, listOf("ml/dL", "mmol/L"), 1)
                }
                "Pulsoksymetr" -> {
                    addNumberEditText("Saturacja tlenu(SpO2)", deviceData, 0)
                    addNumberEditText("Tętno", deviceData, 1)
                }
                else -> {
                    addNumberEditText("Value", deviceData, 0)
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

        private fun addNumberEditText(hint: String, deviceData: DeviceData, index: Int) {
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
                            input.isEmpty() -> "Cannot be empty"
                            !isValidNumber(input) -> "Must be a valid number"
                            else -> null
                        }
                    }
                })
            }

            animateView(editText, index)
            binding.linearLayoutDeviceParam.addView(editText)
        }

        private fun addSpinner(deviceData: DeviceData, items: List<String>, index: Int) {
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

                setSelection(items.indexOf(deviceData.values.getOrNull(index) ?: items.first()))

                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (index < deviceData.values.size) {
                            deviceData.values[index] = items[position]
                        } else {
                            deviceData.values.add(items[position])
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }

            animateView(spinner, index)
            binding.linearLayoutDeviceParam.addView(spinner)
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
    }
}


