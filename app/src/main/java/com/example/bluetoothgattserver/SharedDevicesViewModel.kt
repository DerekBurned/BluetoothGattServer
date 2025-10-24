package com.example.bluetoothgattserver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData

class SharedDevicesViewModel(application: Application) : AndroidViewModel(application) {
    private val _connectedDevices = MutableLiveData<List<BluetoothDomain>>()
    val connectedDevices: LiveData<List<BluetoothDomain>> = _connectedDevices

    fun updateDevices(devices: List<BluetoothDomain>) {
        _connectedDevices.value = devices
    }
}