package com.example.bluetoothgattserver

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class BluetoothStateViewModel(application: MyApplication) : AndroidViewModel(application) {
    private val _bluetoothState = MutableLiveData<Boolean>()

    // Public immutable LiveData for observing
    val bluetoothState: LiveData<Boolean> = _bluetoothState
    fun updateBtState(state: Boolean) {
        _bluetoothState.value = state
    }
}