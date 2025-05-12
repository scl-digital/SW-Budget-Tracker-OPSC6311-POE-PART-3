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
import kotlinx.android.synthetic.main.activity_register.*


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

        firebaseAuth = FirebaseAuth.getInstance()

        binding.registerRegister.setOnClickListener{
            val fname = binding.registerFname.text.toString()
            val uname = binding.registerUsername.text.toString()
            val email = binding.registerEmail.text.toString()
            val password = binding.registerPassword.text.toString()
            val cpassword = binding.registerCpassword.text.toString()

            if (fname.isNotEmpty() && uname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && cpassword.isNotEmpty()){

                if (password == cpassword){
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful){
                            saveUserdata()
                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)
                            this.finish()
                        }else{
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
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