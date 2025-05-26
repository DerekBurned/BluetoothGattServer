package com.example.bluetoothgattserver.Secondactivity

import android.bluetooth.BluetoothDevice


data class DeviceData(
    val name: String,
    val device: BluetoothDevice,
    val values: MutableList<String>
)