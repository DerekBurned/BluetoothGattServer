package com.example.bluetoothgattserver.Secondactivity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.bluetoothgattserver.SharedDevicesViewModel
import com.example.bluetoothgattserver.databinding.ActivitySecondSendBinding

class SecondActivitySend : AppCompatActivity() {
    private lateinit var sharedDevicesViewModel: SharedDevicesViewModel
/*
    private lateinit var adapterSecondActivity:AdapterSecondActvity
*/
    private lateinit var binding:ActivitySecondSendBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySecondSendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedDevicesViewModel = ViewModelProvider(
            applicationContext as ViewModelStoreOwner
        )[SharedDevicesViewModel::class.java]
        sharedDevicesViewModel.connectedDevices.observe(this){
            devices ->

        }


    }
    private fun initViews(){
/*
        binding.recyclerViewItems.adapter = adapterSecondActivity
*/

        binding.buttonSendInfo.setOnClickListener {

        }
    }
}