package com.example.bluetoothgattserver.Secondactivity

import BluetoothServerController.GattServerController
import BluetoothServerController.GattServerManager
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.RenderMode
import com.example.bluetoothgattserver.MainActivity
import com.example.bluetoothgattserver.MyApplication
import com.example.bluetoothgattserver.R
import com.example.bluetoothgattserver.ThirdActivity.ThirdActivity
import com.example.bluetoothgattserver.databinding.ActivitySecondSendBinding

@SuppressLint("MissingPermission")
class SecondActivitySend : AppCompatActivity() {
    private val sharedDevicesViewModel by lazy {
        (application as MyApplication).sharedDevicesViewModel
    }
    private lateinit var adapterSecondActivity: AdapterSecondActvity
    private lateinit var binding: ActivitySecondSendBinding
    private lateinit var serverController: GattServerController
    private val selectedDevices = mutableListOf<Pair<BluetoothDevice, String>>()
    private var previousConnectedDevices: List<Pair<String, BluetoothDevice>> = emptyList()

    private val customOrder = listOf("Ciśniomierz", "Termometr", "Glukometr", "Pulsoksymetr")
    private var isFirstClick = true

    private val listForAdapter = listOf(
        listOf("Ciśnienie skurczowe (SYS)", "Ciśnienie rozkurczowe (DIA)", "Tętno (pul.)"),
        listOf("Temperatura (C°)"),
        listOf("Stężenie glukozy"),
        listOf("% Saturacja tlenu (SpO2)", "Tętno (pul.)")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondSendBinding.inflate(layoutInflater)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        setContentView(binding.root)
        binding.animation.apply {
            setAnimation(R.raw.send_animation)
            post {
                progress = 0f
                alpha = 1f
            }
        }
        initViews()

        sharedDevicesViewModel.connectedDevices.observe(this) { devices ->
            if (devices != null) {
                val filteredAndSorted = devices.filterAndSortByNames(customOrder)


                adapterSecondActivity.updateConnectedDevices(filteredAndSorted)
            }
        }
    }
    fun List<Pair<String, BluetoothDevice>>.filterAndSortByNames(
        allowedNames: List<String>
    ): List<Pair<String, BluetoothDevice>> {
        return this.filter { allowedNames.contains(it.first) }
            .sortedBy { allowedNames.indexOf(it.first) }
    }


    @SuppressLint("ImplicitSamInstance")
    private fun initViews() {
        serverController = GattServerManager.getController()!!
        adapterSecondActivity = AdapterSecondActvity { device, params, isChecked ->

            if (isChecked) {
                Log.d("Adapter Second Activity", "Device selected: ${device?.address}, ${params.toList()}")
                selectedDevices.add(Pair(device, params) as Pair<BluetoothDevice, String>)
            } else {
                selectedDevices.remove(Pair(device, params))
                Log.d("Adapter Second Activity", "Device deselected: ${device?.address}, ${params.toList()}")

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


            Log.d("Selected devices info", "${selectedDevices.toList()}")
            selectedDevices.forEach { (device, inputParams) ->
                serverController.notifyDevice(
                    device.address,
                    inputParams.toByteArray()
                )
            }
            selectedDevices.clear()



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
        binding.imageButton.setOnClickListener {
            setVibrate()
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
        }


    }

    private fun prepareDataToSend(inputParams: List<String>): ByteArray {
        val dataString = inputParams.joinToString(separator = ",")
        return dataString.toByteArray(Charsets.UTF_8)
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


}
