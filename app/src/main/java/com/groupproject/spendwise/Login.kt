package com.groupproject.spendwise

import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.groupproject.spendwise.databinding.ActivityLoginBinding

var userEmail: String = ""

@Suppress("DEPRECATION")
class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("Users")


        binding.loginLogin.setOnClickListener {
            Toast.makeText(this, "Login button clicked", Toast.LENGTH_SHORT).show()
            val email = binding.loginUsername.text.toString()
            val password = binding.loginPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        userEmail = email
                        val intent = Intent(this@Login, Home::class.java)
                        intent.putExtra("userEmail", userEmail)
                        val userKey = userEmail.replace(Regex("[.#$\\[\\]]"), "_")
                        val achievementsRef = FirebaseDatabase.getInstance().getReference("Users").child(userKey).child("achievements")
                        // Initialize all achievements to false if node does not exist
                        achievementsRef.get().addOnSuccessListener { achSnap ->
                            if (!achSnap.exists()) {
                                val initialAchievements = mapOf(
                                    "first_log" to false,
                                    "first_income" to false,
                                    "first_expense" to false,
                                    "consistent_expense_logging" to false
                                )
                                achievementsRef.setValue(initialAchievements)
                            }
                        }
                        // Reset SharedPreferences expense count for this user
                        val prefs = getSharedPreferences("expense_achievements", MODE_PRIVATE)
                        prefs.edit().putInt("expense_count_$userKey", 0).apply()
                        achievementsRef.child("first_log").get().addOnSuccessListener { snap ->
                            if (!(snap.getValue(Boolean::class.java) ?: false)) {
                                achievementsRef.child("first_log").setValue(true)
                                val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                                val channelId = "achievements"
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    val channel = android.app.NotificationChannel(channelId, "Achievements", android.app.NotificationManager.IMPORTANCE_DEFAULT)
                                    notificationManager.createNotificationChannel(channel)
                                }
                                val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
                                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                                    .setContentTitle("Achievement Unlocked!")
                                    .setContentText("First Log In Achievement!")
                                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                                notificationManager.notify(1001, builder.build())
                                androidx.appcompat.app.AlertDialog.Builder(this)
                                    .setTitle("Achievement Unlocked!")
                                    .setMessage("You have unlocked: First Log In!")
                                    .setPositiveButton("OK", null)
                                    .show()
                            }
                        }
                        startActivity(intent)
                        this.finish()
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }



        }

        binding.loginPwreset.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
            this.finish()
        }


    }

    private fun Userlogin(uname: String) {
        dbRef = FirebaseDatabase.getInstance().getReference("Users")

    }


    override fun onBackPressed(){
        startActivity(Intent(this, MainActivity_Welcome::class.java))
        this.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}





