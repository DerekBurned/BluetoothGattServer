package com.example.bluetoothgattserver

import SharedDevicesViewModel
import android.app.Application

class MyApplication : Application() {
    val sharedDevicesViewModel: SharedDevicesViewModel by lazy {
        SharedDevicesViewModel(this)
    }
}