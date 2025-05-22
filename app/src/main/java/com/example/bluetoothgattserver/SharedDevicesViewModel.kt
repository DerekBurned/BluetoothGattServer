package com.example.bluetoothgattserver

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SharedDevicesViewModel(application: Application) : AndroidViewModel(application) {
    private val _connectedDevices = MutableLiveData<List<BluetoothDevice>>()
    val connectedDevices: LiveData<List<BluetoothDevice>> = _connectedDevices

    fun updateDevices(devices: List<BluetoothDevice>) {
        _connectedDevices.postValue(devices)
    }
}