package com.example.bluetoothgattserver

import BluetoothServerController.BluetoothStateListener
import BluetoothServerController.BtStateReceiver
import BluetoothServerController.GattServerListener
import BluetoothServerController.GattServerManager
import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.bluetoothgattserver.Secondactivity.SecondActivitySend
import com.example.bluetoothgattserver.ThirdActivity.ThirdActivity
import com.example.bluetoothgattserver.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity(), GattServerListener, BluetoothStateListener {
    private lateinit var adapterRecycl: ConnectedDevicesAdapter
    private val  bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var binding: ActivityMainBinding
    private var _connectedDevices = mutableListOf<Pair<String, BluetoothDevice>>()
    private val PERMISSION_REQUEST_CODE = 123
    private val sharedDevicesViewModel by lazy {
        (application as MyApplication).sharedDevicesViewModel
    }
    private var isFirstClick = true
    private lateinit var btReceiver: BtStateReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        forceAppTheme(isDark = false)
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ActivityInfo.SCREEN_ORIENTATION_LOCKED
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestBluetoothPermissions()
        btReceiver = BtStateReceiver(this)
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(btReceiver, filter)
        binding.animationArrow.apply {
            setAnimation(R.raw.animation_arrow)
            post {
                progress = 0f
                alpha = 1f
            }
        }
        initViews()

    }



    @SuppressLint("ObsoleteSdkInt")
    private fun initViews() {
        adapterRecycl = ConnectedDevicesAdapter()
        binding.recyclerViewItemsConnectedOrSaved.adapter = adapterRecycl
        lifecycleScope.launch {
            adapterRecycl.submitList(_connectedDevices)
        }
        binding.imageStartServer.setOnClickListener {
            setVibrate()
            val intentthirdAct = Intent(this, ThirdActivity::class.java)
            startActivity(intentthirdAct)
        }
        binding.buttonSend.setOnClickListener {
           setVibrate()
            binding.animationArrow.apply {
                cancelAnimation()
                progress = 0f
            }
            binding.buttonSend.animate()
                .alpha(0f)
                .setDuration(250)
                .withEndAction {
                    binding.buttonSend.visibility = View.INVISIBLE
                    binding.animationArrow.apply {
                        val buttonPos = IntArray(2)
                        binding.buttonSend.getLocationOnScreen(buttonPos)
                        val parentPos = IntArray(2)
                        (parent as View).getLocationOnScreen(parentPos)
                        val xOffset = +300f
                        val yOffset = -60f

                        x = buttonPos[0] - parentPos[0] + xOffset
                        y = buttonPos[1] - parentPos[1] + yOffset

                        visibility = View.VISIBLE
                        bringToFront()
                        requestLayout()

                        if (isFirstClick) {
                            post {
                                playAnimation()
                                isFirstClick = false
                            }
                        } else {
                            playAnimation()
                        }
                    }
                }
                .start()

            binding.animationArrow.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.animationArrow.visibility = View.GONE

                    binding.buttonSend.apply {
                        visibility = View.VISIBLE
                        alpha = 0f
                        animate()
                            .alpha(1f)
                            .setDuration(250)
                            .start()
                    }
                }
            })
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun startSecondActivity() {
        val navigate = Intent(this, SecondActivitySend::class.java)
        GlobalScope.launch(Dispatchers.Main) {
            delay(500)
            startActivity(navigate)
        }

    }

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
                    if (status == BluetoothGatt.GATT_SUCCESS) "succeeded" else "failed"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        GattServerManager.stopServer()
    }

    override fun onResume() {
        super.onResume()
        binding.buttonSend.clearAnimation()
        binding.buttonSend.background = ContextCompat.getDrawable(this, R.drawable.button_neon)
        val marginHorizontal = 16.toPx(this)
        val layoutParams1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams1.setMargins(marginHorizontal, 0, marginHorizontal, marginHorizontal)
        binding.buttonSend.layoutParams = layoutParams1
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
                if(!bluetoothAdapter.isEnabled){
                val enable_Bluetooth = Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE
                )
                startActivityForResult(enable_Bluetooth, 1)
                }else{
                initBtController()
                }
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

    private fun setVibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        200,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
        } else {
            vibrator.vibrate(200)
        }

    }

    override fun onBluetoothTurnedOff() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, 1)
    }

    override fun onBluetoothTurnedOn() {
        Log.d("BtState", "Bluetooth enabled")
        }
}

