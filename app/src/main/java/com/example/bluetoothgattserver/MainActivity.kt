package com.example.bluetoothgattserver

import BluetoothServerController.GattServerController
import BluetoothServerController.GattServerListener
import BluetoothServerController.GattServerManager
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.example.bluetoothgattserver.Secondactivity.SecondActivitySend
import com.example.bluetoothgattserver.ThirdActivity.ThirdActivity
import com.example.bluetoothgattserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity(), GattServerListener {
    private lateinit var adapterRecycl: connectedDevices
    private lateinit var binding: ActivityMainBinding
    private var _connectedDevices = mutableListOf<Pair<String, BluetoothDevice>>()
    private final val PERMISSION_REQUEST_CODE = 123
    private val sharedDevicesViewModel by lazy {
        (application as MyApplication).sharedDevicesViewModel
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forceAppTheme(isDark = false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestBluetoothPermissions()
        initViews()
    }

    private fun initViews() {
        adapterRecycl = connectedDevices{ _, isCHecked ->

        }


        binding.recyclerViewItemsConnectedOrSaved.adapter = adapterRecycl
        lifecycleScope.launch {
            adapterRecycl.submitList(_connectedDevices)
        }
        binding.imageStartServer.setOnClickListener {
        val intentthirdAct = Intent(this,ThirdActivity::class.java)
            startActivity(intentthirdAct)
        }
        binding.buttonSend.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.shrink_and_rotate)
            val layoutParams = binding.buttonSend.layoutParams
            layoutParams.width = 100.toPx(this)
            layoutParams.height = 100.toPx(this)
            binding.buttonSend.layoutParams = layoutParams
            binding.buttonSend.background = ContextCompat.getDrawable(this, R.drawable.round_button)
            it.startAnimation(animation)
            startSecondActivity()
        }
    }

    private fun initBtController() {
        GattServerManager.initialize(this)
        val controller = GattServerManager.getController() ?: return
        controller.setListener(this)

        if (!controller.startServer()) {
            Log.d("Gatt server", "Server is not started")
            finish()
        }
    }

    private fun startSecondActivity() {
        val navigate = Intent(this, SecondActivitySend::class.java)
        GlobalScope.launch(Dispatchers.Main) {
            delay(500)
            startActivity(navigate)
        }
    }


    // MainActivity.kt
    override fun onDeviceConnected(device: BluetoothDevice) {
        runOnUiThread {
            if (_connectedDevices.none { it.second.address == device.address }) {
                val displayName = "Device ${_connectedDevices.size + 1}"
                _connectedDevices.add(Pair(displayName, device))
                sharedDevicesViewModel.updateDevices(_connectedDevices) // Update ViewModel
                adapterRecycl.notifyItemInserted(_connectedDevices.size - 1)
            }
        }
    }

    override fun onDeviceDisconnected(device: BluetoothDevice) {
        runOnUiThread {
            val index = _connectedDevices.indexOfFirst { it.second.address == device.address }
            if (index != -1) {
                _connectedDevices.removeAt(index)
                sharedDevicesViewModel.updateDevices(_connectedDevices) // Update ViewModel
                adapterRecycl.notifyItemRemoved(index)
            }
        }
    }

    // MainActivity.kt
    override fun onDataReceived(device: BluetoothDevice, data: ByteArray) {
        runOnUiThread {
            val message = data.decodeToString().trim()
            val index = _connectedDevices.indexOfFirst { it.second.address == device.address }

            if (message.startsWith("Name:") && index != -1) {
                val newName = message.removePrefix("Name:").trim()
                _connectedDevices[index] = Pair(newName, device)
                sharedDevicesViewModel.updateDevices(_connectedDevices) // Update ViewModel
                adapterRecycl.notifyItemChanged(index)
            }
        }
    }


    override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
            Log.d("BleServer", "MTU for ${device.name} is now $mtu")
        }

        override fun onNotificationSent(device: BluetoothDevice, status: Int) {
            Log.d(
                "BleServer", "Notification to ${device.name} completed with status " +
                        "${if (status == BluetoothGatt.GATT_SUCCESS) "succeeded" else "failed"}"
            )
        }

        override fun onDestroy() {
            super.onDestroy()
            GattServerManager.stopServer()
        }

        private fun hasAllPermissions(): Boolean {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            return permissions.all {
                ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        }

        private fun requestBluetoothPermissions() {
            val permissions = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d("Bluetooth", "✅ All permissions granted.")
                    initBtController()
                } else {
                    Log.d("Bluetooth", "❌ Some permissions were not granted.")
                }
            }
        }

        private fun Int.toPx(context: Context): Int =
            (this * context.resources.displayMetrics.density).toInt()

        fun forceAppTheme(isDark: Boolean) {
            val mode = if (isDark) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(mode)
        }
}

