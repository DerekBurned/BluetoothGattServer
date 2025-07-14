package com.example.bluetoothgattserver

import android.bluetooth.BluetoothDevice

class BluetoothDoman(val name: String, val device: BluetoothDevice) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BluetoothDoman
        return device.address == other.device.address
    }

    override fun hashCode(): Int {
        return device.address.hashCode()
    }
}