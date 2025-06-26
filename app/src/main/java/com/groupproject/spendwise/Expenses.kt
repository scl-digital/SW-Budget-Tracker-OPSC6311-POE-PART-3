package com.groupproject.spendwise

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.groupproject.spendwise.databinding.ActivityExpensesBinding
import java.io.File
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity
import android.view.ViewGroup
import android.graphics.Typeface
import android.util.TypedValue
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat

class Expenses : AppCompatActivity() {
    private lateinit var binding: ActivityExpensesBinding
    private lateinit var expenseRecyclerView: RecyclerView
    private lateinit var expenseAdapter: IncomeAdapter
    private var userEmail: String = ""
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var dbRef: DatabaseReference
    private val expenseList = ArrayList<IncomeModel>()
    private var startDate: String? = null
    private var endDate: String? = null
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userEmail = intent.getStringExtra("userEmail") ?: firebaseAuth.currentUser?.email ?: ""
        val userKey = userEmail.replace(Regex("[.#$\\[\\]]"), "_")
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userKey).child("expense")

        setupRecyclerView()
        setupNavigation()
        loadExpenseData()
        setupDateFilter()

        binding.addExpensesBtn?.setOnClickListener {
            val intent = Intent(this, expense_add_new::class.java)
            intent.putExtra("userEmail", userEmail)
            startActivityForResult(intent, 102)
        }

        binding.expenseLogoutBtn.setOnClickListener {
            userEmail = ""
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        expenseRecyclerView = binding.expenseRecyclerviwe
        expenseRecyclerView.layoutManager = LinearLayoutManager(this)
        expenseRecyclerView.setHasFixedSize(true)
        expenseAdapter = IncomeAdapter(ArrayList())
        expenseRecyclerView.adapter = expenseAdapter
    }

    private fun loadExpenseData() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                expenseList.clear()
                var totalExpense = 0.0
                if (snapshot.exists()) {
                    for (expenseSnapshot in snapshot.children) {
                        val map = expenseSnapshot.value as? Map<*, *> ?: continue
                        val amount = map["amount"]?.toString() ?: "0"
                        val date = map["date"]?.toString() ?: ""
                        val type = map["type"]?.toString() ?: ""
                        val icon = (map["icon"] as? Long)?.toInt() ?: R.drawable.expense_pic
                        val photoUrl = map["photoUrl"]?.toString()
                        expenseList.add(
                            IncomeModel(
                                income = amount,
                                date = date,
                                type = type,
                                icon = icon,
                                description = null,
                                photoUrl = photoUrl
                            )
                        )
                        totalExpense += amount.toDoubleOrNull() ?: 0.0
                    }
                }
                expenseAdapter.updateData(expenseList)
                binding.homeBalance.text = "$%.2f".format(totalExpense)
                // Also update category totals for all
                filterExpenseByDate()
            }
            override fun onCancelled(error: DatabaseError) {
                binding.homeBalance.text = "Error loading data"
            }
        })
    }

    private fun setupNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.bottom_expense
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> navigateTo(Home::class.java)
                R.id.bottom_income -> navigateTo(Income::class.java)
                R.id.bottom_profile -> navigateTo(Profile::class.java)
            }
            true
        }
    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.putExtra("userEmail", userEmail)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onBackPressed() {
        navigateTo(Home::class.java)
    }

    private fun setupDateFilter() {
        val startDateInput = binding.root.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.startDateInput)
        val endDateInput = binding.root.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.endDateInput)
        val filterBtn = binding.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.filterBtn)

        startDateInput.setOnClickListener {
            showDatePicker { date ->
                startDate = date
                startDateInput.setText(date)
            }
        }
        endDateInput.setOnClickListener {
            showDatePicker { date ->
                endDate = date
                endDateInput.setText(date)
            }
        }
        filterBtn.setOnClickListener {
            filterExpenseByDate()
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val dateStr = String.format("%04d/%02d/%02d", year, month + 1, day)
            onDateSelected(dateStr)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun filterExpenseByDate() {
        val categoryTotalsLayout = binding.root.findViewById<LinearLayout>(R.id.categoryTotalsLayout)
        // If no filter, show all
        val filtered = if (startDate.isNullOrEmpty() || endDate.isNullOrEmpty()) {
            expenseList
        } else {
            expenseList.filter {
                val entryDate = it.date ?: return@filter false
                try {
                    val entry = dateFormat.parse(entryDate)
                    val start = dateFormat.parse(startDate!!)
                    val end = dateFormat.parse(endDate!!)
                    entry != null && !entry.before(start) && !entry.after(end)
                } catch (e: Exception) {
                    false
                }
            }
        }
        expenseAdapter.updateData(filtered)
        // Calculate and display category totals
        val categoryTotals = filtered.groupBy { it.type ?: "Other" }
            .mapValues { entry ->
                entry.value.sumOf { it.income?.toDoubleOrNull() ?: 0.0 }
            }
        categoryTotalsLayout.removeAllViews()
        for ((category, total) in categoryTotals) {
            val tv = TextView(this)
            tv.text = "$category: $%.2f".format(total)
            tv.setPadding(24, 12, 24, 12)
            tv.setTextColor(ContextCompat.getColor(this, R.color.white))
            tv.setBackgroundResource(R.drawable.button_bg)
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            tv.setTypeface(tv.typeface, Typeface.BOLD)
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(8, 0, 8, 0)
            tv.layoutParams = params
            categoryTotalsLayout.addView(tv)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 102 && resultCode == RESULT_OK) {
            val userKey = userEmail.replace(Regex("[.#$\\[\\]]"), "_")
            val achievementsRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Users").child(userKey).child("achievements")
            // First expense achievement
            achievementsRef.child("first_expense").get().addOnSuccessListener { snap ->
                if (!(snap.getValue(Boolean::class.java) ?: false)) {
                    achievementsRef.child("first_expense").setValue(true)
                    val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    val channelId = "achievements"
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val channel = android.app.NotificationChannel(channelId, "Achievements", android.app.NotificationManager.IMPORTANCE_DEFAULT)
                        notificationManager.createNotificationChannel(channel)
                    }
                    val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Achievement Unlocked!")
                        .setContentText("First Expense Record Achievement!")
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                    notificationManager.notify(1003, builder.build())
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Achievement Unlocked!")
                        .setMessage("You have unlocked: First Expense Record!")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            // Consistent expense logging achievement (third expense) using SharedPreferences
            val prefs = getSharedPreferences("expense_achievements", MODE_PRIVATE)
            val expenseCount = prefs.getInt("expense_count_$userKey", 0) + 1
            prefs.edit().putInt("expense_count_$userKey", expenseCount).apply()
            if (expenseCount == 3) {
                achievementsRef.child("consistent_expense_logging").get().addOnSuccessListener { achSnap ->
                    if (!(achSnap.getValue(Boolean::class.java) ?: false)) {
                        achievementsRef.child("consistent_expense_logging").setValue(true)
                        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        val channelId = "achievements"
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            val channel = android.app.NotificationChannel(channelId, "Achievements", android.app.NotificationManager.IMPORTANCE_DEFAULT)
                            notificationManager.createNotificationChannel(channel)
                        }
                        val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("Achievement Unlocked!")
                            .setContentText("Consistent Expense Logging Achievement!")
                            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                        notificationManager.notify(1004, builder.build())
                        androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Achievement Unlocked!")
                            .setMessage("You have unlocked: Consistent Expense Logging (3 expenses)!")
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }
        }
    }
}