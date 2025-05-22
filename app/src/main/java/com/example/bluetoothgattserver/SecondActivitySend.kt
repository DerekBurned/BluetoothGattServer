package com.example.bluetoothgattserver

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bluetoothgattserver.databinding.ActivityMainBinding
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