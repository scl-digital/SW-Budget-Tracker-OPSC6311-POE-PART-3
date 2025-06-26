package com.groupproject.spendwise

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.groupproject.spendwise.databinding.ActivityRegisterBinding
import com.google.firebase.FirebaseApp
import android.util.Log


class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_register)

        dbRef = FirebaseDatabase.getInstance().getReference("Users")


        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Log Firebase project info at runtime
        val options = FirebaseApp.getInstance().options
        Log.d("FIREBASE_PROJECT", "Project ID: ${options.projectId}")
        Log.d("FIREBASE_PROJECT", "App ID: ${options.applicationId}")
        Log.d("FIREBASE_PROJECT", "API Key: ${options.apiKey}")

        firebaseAuth = FirebaseAuth.getInstance()

        binding.registerRegister.setOnClickListener{
            val fname = binding.registerFname.text.toString()
            val uname = binding.registerUsername.text.toString()
            val email = binding.registerEmail.text.toString()
            val password = binding.registerPassword.text.toString()
            val cpassword = binding.registerCpassword.text.toString()

            if (fname.isNotEmpty() && uname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && cpassword.isNotEmpty()){

                if (password == cpassword){
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                Log.d("FIREBASE_AUTH", "User registered: $email")
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                                saveUserdata()
                                val intent = Intent(this, Login::class.java)
                                startActivity(intent)
                                this.finish()
                            } else {
                                Log.e("FIREBASE_AUTH", "Registration failed: ${task.exception?.message}")
                                Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener { err ->
                            Log.e("FIREBASE_AUTH", "Registration error: ${err.message}")
                            Toast.makeText(this, "Registration error: ${err.message}", Toast.LENGTH_LONG).show()
                        }
                }else{
                    Toast.makeText(this, "Password does not matched", Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerLogIn.setOnClickListener {
            val loginintent = Intent(this, Login::class.java)
            startActivity(loginintent)
        }


    }

    private fun saveUserdata() {

        //val userId =  dbRef.push().key!!
        val fname = binding.registerFname.text.toString()
        val uname = binding.registerUsername.text.toString()
        val email = binding.registerEmail.text.toString()

        val User = UserModel( email, fname, uname)

        dbRef.child(uname).setValue(User).addOnCompleteListener {
            Toast.makeText(this, "User Created Successful", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { err ->
            Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_SHORT).show()
        }




    }

    override fun onBackPressed(){
        startActivity(Intent(this, MainActivity_Welcome::class.java))
        this.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}