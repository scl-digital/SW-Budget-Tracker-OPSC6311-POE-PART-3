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
import com.groupproject.spendwise.databinding.ActivityIncomeBinding
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

class Income : AppCompatActivity() {
    private lateinit var binding: ActivityIncomeBinding
    private lateinit var incomeRecyclerView: RecyclerView
    private lateinit var incomeAdapter: IncomeAdapter
    private var userEmail: String = ""
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private lateinit var dbRef: DatabaseReference
    private val incomeList = ArrayList<IncomeModel>()
    private var startDate: String? = null
    private var endDate: String? = null
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userEmail = intent.getStringExtra("userEmail") ?: firebaseAuth.currentUser?.email ?: ""
        val userKey = userEmail.replace(Regex("[.#$\\[\\]]"), "_")
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userKey).child("income")

        setupRecyclerView()
        setupNavigation()
        loadIncomeData()
        setupDateFilter()

        binding.addIncomeBtn.setOnClickListener {
            val intent = Intent(this, income_add_new::class.java)
            intent.putExtra("userEmail", userEmail)
            startActivityForResult(intent, 101)
        }

        binding.incomeLogoutBtn.setOnClickListener {
            userEmail = ""
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        incomeRecyclerView = binding.incomeRecyclerview
        incomeRecyclerView.layoutManager = LinearLayoutManager(this)
        incomeRecyclerView.setHasFixedSize(true)
        incomeAdapter = IncomeAdapter(ArrayList())
        incomeRecyclerView.adapter = incomeAdapter
    }

    private fun loadIncomeData() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                incomeList.clear()
                var totalIncome = 0.0
                if (snapshot.exists()) {
                    for (incomeSnapshot in snapshot.children) {
                        val map = incomeSnapshot.value as? Map<*, *> ?: continue
                        val amount = map["amount"]?.toString() ?: "0"
                        val date = map["date"]?.toString() ?: ""
                        val type = map["type"]?.toString() ?: ""
                        val icon = (map["icon"] as? Long)?.toInt() ?: R.drawable.income_pic
                        val photoUrl = map["photoUrl"]?.toString()
                        incomeList.add(
                            IncomeModel(
                                income = amount,
                                date = date,
                                type = type,
                                icon = icon,
                                description = null,
                                photoUrl = photoUrl
                            )
                        )
                        totalIncome += amount.toDoubleOrNull() ?: 0.0
                    }
                }
                incomeAdapter.updateData(incomeList)
                binding.homeBalance.text = "$%.2f".format(totalIncome)
                // Also update category totals for all
                filterIncomeByDate()
            }
            override fun onCancelled(error: DatabaseError) {
                binding.homeBalance.text = "Error loading data"
            }
        })
    }

    private fun setupNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.bottom_income
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> navigateTo(Home::class.java)
                R.id.bottom_expense -> navigateTo(Expenses::class.java)
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
            filterIncomeByDate()
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val dateStr = String.format("%04d/%02d/%02d", year, month + 1, day)
            onDateSelected(dateStr)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun filterIncomeByDate() {
        val categoryTotalsLayout = binding.root.findViewById<LinearLayout>(R.id.categoryTotalsLayout)
        // If no filter, show all
        val filtered = if (startDate.isNullOrEmpty() || endDate.isNullOrEmpty()) {
            incomeList
        } else {
            incomeList.filter {
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
        incomeAdapter.updateData(filtered)
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
        if (requestCode == 101 && resultCode == RESULT_OK) {
            // Check for first income achievement
            val userKey = userEmail.replace(Regex("[.#$\\[\\]]"), "_")
            val achievementsRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Users").child(userKey).child("achievements")
            achievementsRef.child("first_income").get().addOnSuccessListener { snap ->
                if (!(snap.getValue(Boolean::class.java) ?: false)) {
                    achievementsRef.child("first_income").setValue(true)
                    // Show notification
                    val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    val channelId = "achievements"
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        val channel = android.app.NotificationChannel(channelId, "Achievements", android.app.NotificationManager.IMPORTANCE_DEFAULT)
                        notificationManager.createNotificationChannel(channel)
                    }
                    val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("Achievement Unlocked!")
                        .setContentText("First Income Record Achievement!")
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                    notificationManager.notify(1002, builder.build())
                    // Show popup
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Achievement Unlocked!")
                        .setMessage("You have unlocked: First Income Record!")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }
}