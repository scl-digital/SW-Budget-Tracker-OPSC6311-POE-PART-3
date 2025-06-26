package com.groupproject.spendwise

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.groupproject.spendwise.databinding.ActivityMainWelcomeBinding

class MainActivity_Welcome : AppCompatActivity() {
    private lateinit var binding: ActivityMainWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.welcomeLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            this.finish()
        }

        binding.welcomeRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
            this.finish()
        }
    }
}