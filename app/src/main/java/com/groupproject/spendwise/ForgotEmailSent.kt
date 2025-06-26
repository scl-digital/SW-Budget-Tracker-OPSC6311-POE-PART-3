package com.groupproject.spendwise

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.groupproject.spendwise.databinding.ActivityForgotEmailSentBinding

class ForgotEmailSent : AppCompatActivity() {

    private lateinit var binding: ActivityForgotEmailSentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotEmailSentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.forgotemailsentLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            this.finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onBackPressed(){
        startActivity(Intent(this, Login::class.java))
        this.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}