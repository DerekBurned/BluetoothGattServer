package com.example.bluetoothgattserver.Secondactivity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothgattserver.SharedDevicesViewModel
import com.example.bluetoothgattserver.connectedDevices
import com.example.bluetoothgattserver.databinding.ActivitySecondSendBinding

class SecondActivitySend : AppCompatActivity() {
    private val sharedDevicesViewModel: SharedDevicesViewModel by viewModels()
    private lateinit var adapterSecondActivity:AdapterSecondActvity
    private lateinit var binding:ActivitySecondSendBinding
    private val selectedDevices = mutableListOf<BluetoothDevice>()
    private val deviceParameters = mutableMapOf<String, List<String>>()
    private lateinit var connectedDevices: MutableList<BluetoothDevice>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondSendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initializeDeviceParameters()
        sharedDevicesViewModel.connectedDevices.observe(this){
                devices ->
            connectedDevices = devices.toMutableList()


        }


    }
    private fun initializeDeviceParameters() {
        deviceParameters["Ciśniomierz"] = listOf("Sys", "Dia", "Pul")
        deviceParameters["Termometr"] = listOf("Temp")
        deviceParameters["Glukometr"] = listOf("Glukoza")
        deviceParameters["Pulsoksymetr"] = listOf("SpO2", "Pul")
    }
    private fun initViews(){
        val bluetoothDevices = connectedDevices

        adapterSecondActivity = AdapterSecondActvity(
            onDeviceCheck = { device, isChecked ->
                // Handle device selection
                if (isChecked) {
                    selectedDevices.add(device)
                } else {
                    selectedDevices.remove(device)
                }
            },
            infoOnDevice = { params, device ->
                // Handle parameter information for the device
                showParameterDialog(params, device)
            }
        )

        binding.recyclerViewItems.apply {
            layoutManager = LinearLayoutManager(this@SecondActivitySend)
            adapter = adapterSecondActivity
            itemAnimator = DefaultItemAnimator()
        }

        // Submit your device list to the adapter
        adapterSecondActivity.submitList(bluetoothDevices)
    }

    }
@SuppressLint("MissingPermission")
private fun showParameterDialog(params: List<String>, device: BluetoothDevice) {
    // Create a dialog or new activity to show/edit parameters
    val dialog = AlertDialog.Builder(this)
        .setTitle("Enter Parameters for ${device.name}")
        .setView(createParameterInputViews(params))
        .setPositiveButton("Save") { dialog, _ ->
            // Handle saving parameters
            saveParametersForDevice(device)
            dialog.dismiss()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .create()

    dialog.show()
}
private fun createParameterInputViews(params: List<String>): View {
    val layout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(32, 32, 32, 32)
    }


    params.forEach { paramName ->
        val input = EditText(this).apply {
            hint = paramName
            // Set appropriate input type based on parameter
            inputType = when {
                paramName.contains("Temp", ignoreCase = true) ->
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                paramName.contains("Pul", ignoreCase = true) ->
                    InputType.TYPE_CLASS_NUMBER
                else -> InputType.TYPE_CLASS_TEXT
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }

            // Add tag to identify which parameter this is
            tag = paramName
        }

        layout.addView(input)
    }

    return layout
}
private fun getParametersForDevice(device: BluetoothDevice): Map<String, String> {
    // Implement this to retrieve saved parameters for the device
    return emptyMap()
}

private fun sendDataToDevices(data: List<Map<String, Any>>) {
    // Implement your BLE sending logic here
    data.forEach { deviceData ->
        Log.d("SendingData", "Device: ${deviceData["device"]}")
        (deviceData["parameters"] as? Map<*, *>)?.forEach { (key, value) ->
            Log.d("SendingData", "$key: $value")
        }
    }
}
private fun saveParametersForDevice(device: BluetoothDevice) {
    // Implement your parameter saving logic here
    // You'll need to get references to the EditText views and save their values
}
}