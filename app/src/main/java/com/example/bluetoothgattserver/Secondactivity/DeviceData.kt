package com.example.bluetoothgattserver.Secondactivity

import android.bluetooth.BluetoothDevice

data class DeviceData(
    val name: String,
    val device: BluetoothDevice,
    val values: MutableList<String> = when (name) {
        "Ciśniomierz" -> MutableList(3) { "" }
        "Termometr" -> MutableList(1) { "" }
        "Glukometr" -> MutableList(2) { "" } // First for value, second for meal time
        "Pulsoksymetr" -> MutableList(2) { "" }
        else -> MutableList(1) { "" }
    }
)