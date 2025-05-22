package com.example.bluetoothgattserver

import BluetoothServerController.GattServerController
import BluetoothServerController.GattServerManager
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.example.bluetoothgattserver.Secondactivity.SecondActivitySend
import com.example.bluetoothgattserver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity(), GattServerController.GattServerListener {
    private lateinit var adapterRecycl:connectedDevices
    private lateinit var binding:ActivityMainBinding


    private var _connectedDevices = mutableListOf<BluetoothDevice>()
    private lateinit var sharedDevicesViewModel: SharedDevicesViewModel
    companion object
    private final val PERMISSION_REQUEST_CODE = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestBluetoothPermissions()
        initViews()
        sharedDevicesViewModel = ViewModelProvider(this.applicationContext as ViewModelStoreOwner)[SharedDevicesViewModel::class.java]
    }
    private fun initViews(){
        adapterRecycl = connectedDevices{device, isChecked ->
            if (isChecked) {

                Log.d("Adapter", "Selected device: ${device.name} - ${device.address}")
            } else {

                Log.d("Adapter", "Deselected device: ${device.name}")
            }
        }

        binding.recyclerViewItemsConnectedOrSaved.adapter = adapterRecycl
        lifecycleScope.launch {
            adapterRecycl.submitList(_connectedDevices)
        }
        binding.imageStartServer.setOnClickListener {
            initBtController()
        }
        binding.buttonSend.setOnClickListener{
            val animation = AnimationUtils.loadAnimation( this,R.anim.rotate)
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

    private fun startSecondActivity(){
        val navigate = Intent(this, SecondActivitySend::class.java)
       GlobalScope.launch(Dispatchers.Main) {
           delay(1000)
           startActivity(navigate)
       }    }


    override fun onDeviceConnected(device: BluetoothDevice) {
        runOnUiThread {
            if(!_connectedDevices.contains(device)){
                _connectedDevices.add(device)
                sharedDevicesViewModel.updateDevices(_connectedDevices)
                adapterRecycl.notifyItemInserted(_connectedDevices.size-1)

            }
            Log.d("Connection" ,"Device connected ${device.name}, ${device.address}")

        }
    }

    override fun onDeviceDisconnected(device: BluetoothDevice) {
        runOnUiThread {
            Log.d("Connection" ,"Device disconnected ${device.name}, ${device.address}")
            val index = _connectedDevices.indexOfFirst { it.address == device.address }
            if (index != -1) {
                _connectedDevices.removeAt(index)
                sharedDevicesViewModel.updateDevices(_connectedDevices)
                adapterRecycl.notifyItemRemoved(index)
            }
        }
    }

    override fun onDataReceived(device: BluetoothDevice, data: ByteArray) {
        runOnUiThread {
            val message = "From ${device.name}: ${data.decodeToString()}"
            Log.d("Message", "$message")
        }
    }

    override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
        Log.d("BleServer", "MTU for ${device.name} is now $mtu")
    }

    override fun onNotificationSent(device: BluetoothDevice, status: Int) {
        Log.d("BleServer", "Notification to ${device.name} completed with status " +
                "${if(status== BluetoothGatt.GATT_SUCCESS)"succeeded" else "failed"}")
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
}