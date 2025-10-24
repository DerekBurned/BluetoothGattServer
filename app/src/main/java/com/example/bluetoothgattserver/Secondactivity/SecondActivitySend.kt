package com.example.bluetoothgattserver.Secondactivity

import BluetoothServerController.GattServerController
import BluetoothServerController.GattServerManager
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothgattserver.BluetoothDomain
import com.example.bluetoothgattserver.MyApplication
import com.example.bluetoothgattserver.R
import com.example.bluetoothgattserver.ThirdActivity.ThirdActivity
import com.example.bluetoothgattserver.databinding.ActivitySecondSendBinding

@SuppressLint("MissingPermission")
class SecondActivitySend : AppCompatActivity() {
    private val sharedDevicesViewModel by lazy {
        (application as MyApplication).sharedDevicesViewModel
    }
    private val bluetoothStateViewModel by lazy {
        (application as MyApplication).bluetoothStateViewModel
    }
    private lateinit var adapterSecondActivity: AdapterSecondActvity
    private lateinit var binding: ActivitySecondSendBinding
    private lateinit var serverController: GattServerController
    private val selectedDevices = mutableListOf<Pair<BluetoothDomain, String>>()
    private val customOrder = listOf("Ciśnieniomierz", "Termometr", "Glukometr", "Pulsoksymetr")
    private var isFirstClick = true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondSendBinding.inflate(layoutInflater)
       setContentView(binding.root)
        binding.animation.apply {
            setAnimation(R.raw.send_animation)
            post {
                progress = 0f
                alpha = 1f
            }
        }
        initViews()

        bluetoothStateViewModel.bluetoothState.observe(this){
            state ->
            when (state) {
                true -> binding.imageButton.imageTintList = ColorStateList.valueOf(Color.GREEN)
                false -> binding.imageButton.imageTintList = ColorStateList.valueOf(Color.RED)
            }
        }
        sharedDevicesViewModel.connectedDevices.observe(this) { devices ->
            if (devices != null) {
                val filteredAndSorted = devices.filterAndSortByNames(customOrder)


                adapterSecondActivity.updateConnectedDevices(filteredAndSorted)
            }
        }
    }
    fun List<BluetoothDomain>.filterAndSortByNames(
        allowedNames: List<String>
    ): List<BluetoothDomain> {
        return this.filter { allowedNames.contains(it.name) }
            .sortedBy { allowedNames.indexOf(it.name) }
    }


    @SuppressLint("ImplicitSamInstance")
    private fun initViews() {
        serverController = GattServerManager.getController()!!
        adapterSecondActivity = AdapterSecondActvity { device, params, isChecked ->
            if (isChecked) {
                device?.let { nonNullDevice ->  // Only add if device is not null
                    Log.d("Adapter Second Activity", "Device selected: ${nonNullDevice.device.address}, ${params.toList()}")
                    selectedDevices.add(nonNullDevice to params)
                }
            } else {
                device?.let { nonNullDevice ->
                    selectedDevices.removeAll { it.first == nonNullDevice }
                    Log.d("Adapter Second Activity", "Device deselected: ${nonNullDevice.device.address}, ${params.toList()}")
                }
            }
        }

        binding.recyclerViewItems.apply {
            layoutManager = LinearLayoutManager(this@SecondActivitySend)
            adapter = adapterSecondActivity
            itemAnimator = DefaultItemAnimator()
        }

        binding.buttonSendInfo.setOnClickListener {
            setVibrate()
            binding.animation.apply {
                cancelAnimation()
                progress = 0f
            }
            binding.buttonSendInfo.animate()
                .alpha(0f)
                .setDuration(250)
                .withEndAction {
                    binding.buttonSendInfo.visibility = View.INVISIBLE
                    binding.animation.apply {
                        val buttonPos = IntArray(2)
                        binding.buttonSendInfo.getLocationOnScreen(buttonPos)
                        val parentPos = IntArray(2)
                        (parent as View).getLocationOnScreen(parentPos)
                        val xOffset = -40f
                        val yOffset = -260f

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

            filterToKeepLastMessages()
            Log.d("Selected devices info", "${selectedDevices.toList()}")
            selectedDevices.forEach { (device, inputParams) ->
                serverController.notifyDevice(
                    device.device.address,
                    inputParams.toByteArray()
                )
            }



            binding.animation.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.animation.visibility = View.GONE

                    binding.buttonSendInfo.apply {
                        visibility = View.VISIBLE
                        alpha = 0f
                        animate()
                            .alpha(1f)
                            .setDuration(250)
                            .start()
                    }
                }
            })
        }
        binding.buttonAbout.setOnClickListener {
            setVibrate()
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapterSecondActivity.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        adapterSecondActivity.restoreState(savedInstanceState)

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

    fun filterToKeepLastMessages() {
        val uniqueByDevice = selectedDevices
            .asReversed() // reverse to prioritize last entries
            .distinctBy { it.first } // keep first occurrence of each device (which is the last message due to reversal)
            .asReversed() // optional: re-reverse to keep original order

        selectedDevices.clear()
        selectedDevices.addAll(uniqueByDevice)
    }


}
