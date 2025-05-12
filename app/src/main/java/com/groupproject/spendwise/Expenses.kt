package com.groupproject.spendwise


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

import com.groupproject.spendwise.databinding.ActivityExpensesBinding
import kotlinx.android.synthetic.main.activity_expenses.*


class Expenses : AppCompatActivity() {

    private lateinit var binding: ActivityExpensesBinding
    private lateinit var dbRef: DatabaseReference
    private lateinit var incomeRecyclerView: RecyclerView
    private lateinit var expenseArrayList : ArrayList<IncomeModel>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = username
        dbRef = FirebaseDatabase.getInstance().getReference(username + "expense")


        incomeRecyclerView = findViewById(R.id.expense_recyclerviwe)
        incomeRecyclerView.layoutManager = LinearLayoutManager(this)
        incomeRecyclerView.setHasFixedSize(true)


        expenseArrayList = arrayListOf<IncomeModel>()

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){

                    for (incomeSnapshot in snapshot.children){

                        val expense = incomeSnapshot.getValue(IncomeModel::class.java)
                        expenseArrayList.add(expense!!)

                    }

                    incomeRecyclerView.adapter = IncomeAdapter(expenseArrayList)

                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })






        add_expenses_btn.setOnClickListener {
            startActivity(Intent(this, expense_add_new::class.java))
            intent.putExtra("username", username)
            this.finish()
        }




        binding.bottomNavigationView.setSelectedItemId(R.id.bottom_expense)
        binding.bottomNavigationView.setOnItemSelectedListener {
            if (it.itemId == R.id.bottom_home){
                val intent = Intent(this, Home::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                this.finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                true

            }else if (it.itemId == R.id.bottom_income){
                val intent = Intent(this, Income::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                this.finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                true
            }else if (it.itemId == R.id.bottom_expense){
                true
            }else if (it.itemId == R.id.bottom_profile){
                val intent = Intent(this, Profile::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                this.finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                true
            }

            true
        }

        expense_logout_btn.setOnClickListener(View.OnClickListener{
            startActivity(Intent(this, Login::class.java))
            this.finish()
        })




    }

    override fun onBackPressed(){
        startActivity(Intent(this, Home::class.java))

        this.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}