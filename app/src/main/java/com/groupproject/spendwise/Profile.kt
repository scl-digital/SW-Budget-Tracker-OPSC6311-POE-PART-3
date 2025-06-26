package com.groupproject.spendwise

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.groupproject.spendwise.databinding.ActivityProfileBinding
import com.google.firebase.database.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.groupproject.spendwise.Badge
import com.groupproject.spendwise.BadgeAdapter




class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set userEmail from intent if present
        intent.getStringExtra("userEmail")?.let {
            if (it.isNotEmpty()) userEmail = it
        }
        val userKey = userEmail.replace(Regex("""[.#$\[\]]"""), "_")

        // Set email TextView
        binding.textViewprofileemail.text = userEmail

        // Fetch username from Firebase
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.orderByChild("email").equalTo(userEmail).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val userSnap = snapshot.children.first()
                val uname = userSnap.child("uname").getValue(String::class.java) ?: ""
                binding.textViewProfilename.text = uname
            } else {
                binding.textViewProfilename.text = ""
            }
        }.addOnFailureListener {
            binding.textViewProfilename.text = ""
        }

        binding.bottomNavigationView.setSelectedItemId(R.id.bottom_profile)
        binding.bottomNavigationView.setOnItemSelectedListener {
            if (it.itemId == R.id.bottom_home){
                val intent = Intent(this, Home::class.java)
                intent.putExtra("userEmail", userEmail)
                startActivity(intent)
                this.finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                true

            }else if (it.itemId == R.id.bottom_income){
                val intent = Intent(this, Income::class.java)
                intent.putExtra("userEmail", userEmail)
                startActivity(intent)
                this.finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                true
            }else if (it.itemId == R.id.bottom_expense){
                val intent = Intent(this, Expenses::class.java)
                intent.putExtra("userEmail", userEmail)
                startActivity(intent)
                this.finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                true
            }else if (it.itemId == R.id.bottom_profile){
                true

            }

            true
        }

        binding.profileLogoutBtn.setOnClickListener {
            userEmail = ""
            startActivity(Intent(this, Login::class.java))
            intent.putExtra("userEmail", userEmail)
            this.finish()
        }

        // --- BADGES/ACHIEVEMENTS ---
        val badgesRecyclerView = binding.badgesRecyclerView
        badgesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val achievementsRef = FirebaseDatabase.getInstance().getReference("Users").child(userKey).child("achievements")
        achievementsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firstLog = snapshot.child("first_log").getValue(Boolean::class.java) ?: false
                val firstIncome = snapshot.child("first_income").getValue(Boolean::class.java) ?: false
                val firstExpense = snapshot.child("first_expense").getValue(Boolean::class.java) ?: false
                val consistentExpense = snapshot.child("consistent_expense_logging").getValue(Boolean::class.java) ?: false
                val badges = listOf(
                    Badge(R.drawable.ic_launcher_foreground, "First Log", firstLog),
                    Badge(R.drawable.ic_launcher_foreground, "First Income", firstIncome),
                    Badge(R.drawable.ic_launcher_foreground, "First Expense", firstExpense),
                    Badge(R.drawable.ic_launcher_foreground, "Consistent Expense Logging", consistentExpense)
                )
                badgesRecyclerView.adapter = BadgeAdapter(badges)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        // --- END BADGES/ACHIEVEMENTS ---

    }

    override fun onBackPressed(){
        startActivity(Intent(this, Home::class.java))

        this.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}