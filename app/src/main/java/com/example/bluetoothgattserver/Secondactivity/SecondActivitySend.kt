package com.example.bluetoothgattserver.Secondactivity

import BluetoothServerController.GattServerController
import BluetoothServerController.GattServerManager
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothgattserver.MyApplication
import com.example.bluetoothgattserver.databinding.ActivitySecondSendBinding

@SuppressLint("MissingPermission")
class SecondActivitySend : AppCompatActivity() {
    private val sharedDevicesViewModel by lazy {
        (application as MyApplication).sharedDevicesViewModel
    }
    private lateinit var adapterSecondActivity: AdapterSecondActvity
    private lateinit var binding: ActivitySecondSendBinding
    private lateinit var serverController: GattServerController
    private val selectedDevices = mutableListOf<Pair<BluetoothDevice, List<String>>>()
    private val customOrder = listOf("Ciśniomierz", "Termometr", "Glukometr", "Pulsoksymetr")

    private val listForAdapter = listOf(
        listOf("Ciśnienie skurczowe (SYS)", "Ciśnienie rozkurczowe (DIA)", "Tętno (pul.)"),
        listOf("Temperatura (C°)"),
        listOf("Stężenie glukozy"),
        listOf("% Saturacja tlenu (SpO2)", "Tętno (pul.)")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondSendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()

        sharedDevicesViewModel.connectedDevices.observe(this) { devices ->
            if (devices != null) {
                // Update your RecyclerView here
                val sorted = devices.sortedBy { customOrder.indexOf(it.first) }
                val deviceDataList = sorted.map { devicePair ->
                    DeviceData(
                        name = devicePair.first,
                        device = devicePair.second,
                        values = mutableListOf() // Adjust as needed
                    )
                }
                adapterSecondActivity.submitList(deviceDataList)
            }
        }
    }

    private fun initListToAdapter(
        devices: List<Pair<String, BluetoothDevice>>,
        strings: List<List<String>>
    ): List<Pair<Pair<String, BluetoothDevice>, List<String>>> {
        val adjustedStrings = strings.take(devices.size)
        return devices.zip(adjustedStrings).map { (devicePair, params) ->
            Pair(devicePair, params)
        }
    }

    @SuppressLint("ImplicitSamInstance")
    private fun initViews() {
        serverController = GattServerManager.getController()!!
        adapterSecondActivity = AdapterSecondActvity { device, params, isChecked ->

            if (isChecked) {
                selectedDevices.add(Pair(device, params))
            } else {
                selectedDevices.remove(Pair(device, params))
            }


        }

        binding.recyclerViewItems.apply {
            layoutManager = LinearLayoutManager(this@SecondActivitySend)
            adapter = adapterSecondActivity
            itemAnimator = DefaultItemAnimator()
        }
        binding.buttonSendInfo.setOnClickListener {
            for (devicePair in selectedDevices) {
                val device = devicePair.first
                val inputParams = devicePair.second

                // Validate and prepare data bytes to send
                val dataToSend = prepareDataToSend(inputParams)

                // Send to device by its address
                val success = serverController.notifyDevice(device.address, dataToSend)
                if (!success) {
                    Log.e("SecondActivitySend", "Failed to send data to device: ${device.address}")
                } else {
                    Log.i("SecondActivitySend", "Data sent to device: ${device.address}")
                }
            }
        }


    }
    private fun prepareDataToSend(inputParams: List<String>): ByteArray {
        // For example, join inputs with commas
        val dataString = inputParams.joinToString(separator = ",")
        return dataString.toByteArray(Charsets.UTF_8)
    }

}
