package com.example.bluetoothgattserver.Secondactivity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.bluetoothgattserver.SharedDevicesViewModel
import com.example.bluetoothgattserver.databinding.ActivitySecondSendBinding

class SecondActivitySend : AppCompatActivity() {
    private val sharedDevicesViewModel: SharedDevicesViewModel by viewModels()/*
    private lateinit var adapterSecondActivity:AdapterSecondActvity
*/
    private lateinit var binding:ActivitySecondSendBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySecondSendBinding.inflate(layoutInflater)
        setContentView(binding.root)
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