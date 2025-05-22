package com.example.bluetoothgattserver.Secondactivity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bluetoothgattserver.databinding.ActivitySecondSendBinding

class SecondActivitySend : AppCompatActivity() {
    private lateinit var binding:ActivitySecondSendBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySecondSendBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}