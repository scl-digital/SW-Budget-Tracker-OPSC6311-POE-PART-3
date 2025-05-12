package com.groupproject.spendwise

import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.groupproject.spendwise.databinding.ActivityLoginBinding

var username: String = ""

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

            val uname = binding.loginUsername.text.toString()
            val password = binding.loginPassword.text.toString()
            username = uname



            dbRef.child(uname).get().addOnSuccessListener{
                val email = it.child("email").value.toString()
                if (uname.isNotEmpty() && password.isNotEmpty()){

                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful){

                            val intent = Intent(this@Login, Home::class.java)
                            intent.putExtra("username", uname)
                            startActivity(intent)
                            this.finish()



                        }else{
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                }



            }.addOnFailureListener {
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
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





