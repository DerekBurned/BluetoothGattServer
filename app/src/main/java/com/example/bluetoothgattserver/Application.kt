package com.example.bluetoothgattserver
// MyApplication.kt
import SharedDevicesViewModel
import android.app.Application

class MyApplication : Application() {
    val sharedDevicesViewModel: SharedDevicesViewModel by lazy {
        SharedDevicesViewModel(this)
    }
}