package com.example.bluetoothgattserver.Secondactivity

import android.bluetooth.BluetoothDevice

data class DeviceData(
    val name: String,
    val device: BluetoothDevice,
    val values: MutableList<String> = when (name) {
        "Ciśniomierz" -> MutableList(3) { "" }
        "Termometr" -> MutableList(1) { "" }
        "Glukometr" -> MutableList(2) { "" }
        "Pulsoksymetr" -> MutableList(2) { "" }
        else -> MutableList(1) { "" }
    }
) {
    val deviceId: String get() = device.address
}
