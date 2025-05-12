package com.groupproject.spendwise

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.groupproject.spendwise.databinding.ActivityExpenseAddNewBinding
import kotlinx.android.synthetic.main.activity_expense_add_new.*
import kotlinx.android.synthetic.main.activity_income_add_new.*
import java.util.*


var expensetype:String = ""
var expenseicon:Int = 0

class expense_add_new : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseAddNewBinding
    private lateinit var dbRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseAddNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = username

        val expensetypes = listOf("Bills", "Education", "Health", "Transportation", "Food", "Other")
        val autocomplete : AutoCompleteTextView = findViewById(R.id.expense_drop_items)



        dbRef = FirebaseDatabase.getInstance().getReference(username + "expense")


        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)


        expense_date.setOnClickListener {
            val datepicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { datePicker, syear, smonth, sday ->
                expense_date.setText("" + syear +"/"+ smonth +"/"+ sday)
            }, year, month, day)
            datepicker.show()
        }


        val adapter = ArrayAdapter(this, R.layout.list_items_income, expensetypes)
        autocomplete.setAdapter(adapter)
        autocomplete.onItemClickListener = AdapterView.OnItemClickListener {
                adapterView, view, i, l ->
            val itemselected = adapterView.getItemAtPosition(i)
            expensetype = itemselected.toString()
            if (expensetype=="Bills"){
                expenseicon = R.drawable.bills
            }else if (expensetype=="Education"){
                expenseicon = R.drawable.education
            }else if (expensetype=="Health"){
                expenseicon = R.drawable.health
            }else if (expensetype=="Transportation"){
                expenseicon = R.drawable.transportation
            }else if (expensetype=="Food"){
                expenseicon = R.drawable.food
            }else if (expensetype=="Other"){
                expenseicon = R.drawable.other_expenses
            }
        }



        add_expenses_bacK.setOnClickListener {
            startActivity(Intent(this, Expenses::class.java))
            intent.putExtra("username", username)
            this.finish()
        }


        binding.addNewExpense.setOnClickListener {
            val amount = binding.expenseAmount.text.toString()
            val date = binding.expenseDate.text.toString()
            val extype = binding.expenseType.toString()

            if (amount.isNotEmpty() && date.isNotEmpty() && extype.isNotEmpty()){

                val expensevalue = IncomeModel( amount, date, expensetype, expenseicon)

                dbRef.child(amount).setValue(expensevalue).addOnCompleteListener {
                    Toast.makeText(this, "Expense Added Successful", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { err ->
                    Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_SHORT).show()
                }
                startActivity(Intent(this, Expenses::class.java))
                intent.putExtra("username", username)
                this.finish()

            }else{
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }



        }





    }
}