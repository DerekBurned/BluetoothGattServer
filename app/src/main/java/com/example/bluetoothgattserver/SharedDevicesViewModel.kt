package com.example.bluetoothgattserver

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData

class SharedDevicesViewModel(application: Application) : AndroidViewModel(application) {
    private val _connectedDevices = MutableLiveData<List<BluetoothDoman>>()
    val connectedDevices: LiveData<List<BluetoothDoman>> = _connectedDevices

    fun updateDevices(devices: List<BluetoothDoman>) {
        _connectedDevices.value = devices
    }
}