package com.example.bluetoothgattserver.ThirdActivity

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieDrawable
import com.example.bluetoothgattserver.R
import com.example.bluetoothgattserver.databinding.ActivityThirdBinding

class ThirdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThirdBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ActivityInfo.SCREEN_ORIENTATION_LOCKED

        binding.animationCat.apply {
            repeatCount = LottieDrawable.INFINITE
            playAnimation()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.animationCat.resumeAnimation()
    }

    override fun onPause() {
        super.onPause()
        binding.animationCat.pauseAnimation()
    }
}