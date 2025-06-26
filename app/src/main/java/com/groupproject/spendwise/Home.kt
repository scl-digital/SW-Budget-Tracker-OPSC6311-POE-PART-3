package com.groupproject.spendwise

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.groupproject.spendwise.databinding.ActivityHomeBinding
import com.google.firebase.database.*
import android.widget.Toast
import android.graphics.Color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageView

class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var userEmail: String = ""
    private var totalIncome = 0.0
    private var totalExpense = 0.0
    private var minGoal: Double? = null
    private var maxGoal: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set userEmail from intent if present
        intent.getStringExtra("userEmail")?.let {
            if (it.isNotEmpty()) userEmail = it
        }
        val userKey = userEmail.replace(Regex("""[.#$\[\]]"""), "_")
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userKey)
        val goalsRef = userRef.child("goals")

        // --- GOALS: Load and display min/max goals ---
        val minGoalInput = binding.include2.root.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.minGoalInput)
        val maxGoalInput = binding.include2.root.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.maxGoalInput)
        val saveGoalBtn = binding.include2.root.findViewById<com.google.android.material.button.MaterialButton>(R.id.saveGoalBtn)

        // Load goals from Firebase
        goalsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                minGoal = snapshot.child("min").getValue(Double::class.java)
                maxGoal = snapshot.child("max").getValue(Double::class.java)
                minGoalInput.setText(minGoal?.toString() ?: "")
                maxGoalInput.setText(maxGoal?.toString() ?: "")
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Save goals to Firebase
        saveGoalBtn.setOnClickListener {
            val minGoal = minGoalInput.text?.toString()?.toDoubleOrNull()
            val maxGoal = maxGoalInput.text?.toString()?.toDoubleOrNull()
            if (minGoal == null || maxGoal == null) {
                Toast.makeText(this, "Please enter valid min and max goals", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val goalMap = mapOf("min" to minGoal, "max" to maxGoal)
            goalsRef.setValue(goalMap).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Goals saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save goals", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // --- NEW: Fetch and display totals ---
        val incomeRef = userRef.child("income")
        val expenseRef = userRef.child("expense")

        // Listen for goal changes
        goalsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                minGoal = snapshot.child("min").getValue(Double::class.java)
                maxGoal = snapshot.child("max").getValue(Double::class.java)
                // After loading, update balance color if needed
                updateBalanceColor()
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Fetch income
        incomeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalIncome = 0.0
                for (incomeSnap in snapshot.children) {
                    val incomeStr = incomeSnap.child("amount").getValue(String::class.java)
                    val amount = incomeStr?.toDoubleOrNull() ?: 0.0
                    totalIncome += amount
                }
                binding.include2.homeIncome.text = "$%.2f".format(totalIncome)
                val balance = totalIncome - totalExpense
                binding.include2.homeBalance.text = "$%.2f".format(balance)
                updateBalanceColor()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Home, "Failed to load income", Toast.LENGTH_SHORT).show()
            }
        })

        // Fetch expenses
        expenseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalExpense = 0.0
                for (expenseSnap in snapshot.children) {
                    val expenseStr = expenseSnap.child("amount").getValue(String::class.java)
                    val amount = expenseStr?.toDoubleOrNull() ?: 0.0
                    totalExpense += amount
                }
                binding.include2.homeExpense.text = "$%.2f".format(totalExpense)
                val balance = totalIncome - totalExpense
                binding.include2.homeBalance.text = "$%.2f".format(balance)
                updateBalanceColor()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Home, "Failed to load expenses", Toast.LENGTH_SHORT).show()
            }
        })
        // --- END NEW ---

        // --- ANALYTICS CHART ---
        val barChart = binding.include2.root.findViewById<BarChart>(R.id.categoryBarChart)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.MONTH, -1)
        val startDate = calendar.time
        expenseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryTotals = mutableMapOf<String, Double>()
                for (expenseSnap in snapshot.children) {
                    val amountStr = expenseSnap.child("amount").getValue(String::class.java)
                    val amount = amountStr?.toDoubleOrNull() ?: 0.0
                    val category = expenseSnap.child("type").getValue(String::class.java) ?: "Other"
                    val dateStr = expenseSnap.child("date").getValue(String::class.java)
                    val entryDate = try { dateFormat.parse(dateStr ?: "") } catch (e: Exception) { null }
                    if (entryDate != null && !entryDate.before(startDate) && !entryDate.after(endDate)) {
                        categoryTotals[category] = (categoryTotals[category] ?: 0.0) + amount
                    }
                }
                // Prepare chart data
                val entries = mutableListOf<BarEntry>()
                val categories = categoryTotals.keys.toList()
                categoryTotals.values.forEachIndexed { idx, total ->
                    entries.add(BarEntry(idx.toFloat(), total.toFloat()))
                }
                val dataSet = BarDataSet(entries, "Spending by Category")
                dataSet.setColors(*com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS)
                val barData = BarData(dataSet)
                barData.barWidth = 0.7f
                barChart.data = barData
                // X Axis labels
                val xAxis = barChart.xAxis
                xAxis.valueFormatter = IndexAxisValueFormatter(categories)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                xAxis.labelCount = categories.size
                // Y Axis
                val yAxis = barChart.axisLeft
                yAxis.setDrawGridLines(true)
                barChart.axisRight.isEnabled = false
                // Add min/max goal lines if set
                yAxis.removeAllLimitLines()
                minGoal?.let {
                    val minLine = LimitLine(it.toFloat(), "Min Goal")
                    minLine.lineColor = android.graphics.Color.parseColor("#FF9800")
                    minLine.lineWidth = 2f
                    yAxis.addLimitLine(minLine)
                }
                maxGoal?.let {
                    val maxLine = LimitLine(it.toFloat(), "Max Goal")
                    maxLine.lineColor = android.graphics.Color.parseColor("#F44336")
                    maxLine.lineWidth = 2f
                    yAxis.addLimitLine(maxLine)
                }
                barChart.description.isEnabled = false
                barChart.legend.isEnabled = true
                barChart.invalidate()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        // --- END ANALYTICS CHART ---

        // --- FINANCIAL TIPS SLIDER ---
        val tips = listOf(
            Pair("Financial Tip #1", "💰 Pay yourself first! Automatically transfer a portion of your income to savings before spending."),
            Pair("Financial Tip #2", "📝 Track every expense. Small purchases add up—record them to see where your money goes."),
            Pair("Financial Tip #3", "🎯 Set clear, achievable goals. Visualize your progress to stay motivated!"),
            Pair("Financial Tip #4", "🔄 Review your budget monthly. Adjust as your needs and priorities change."),
            Pair("Financial Tip #5", "⏳ Wait 24 hours before making non-essential purchases to avoid impulse buys."),
            Pair("Financial Tip #6", "💳 Use cash for fun spending. It's easier to stick to your budget when you see the money leave your wallet!"),
            Pair("Financial Tip #7", "⏰ Automate bill payments to avoid late fees and protect your credit score."),
            Pair("Financial Tip #8", "🛡️ Build an emergency fund. Aim for 3–6 months of expenses for peace of mind.")
        )
        val tipsRecyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.tipsRecyclerView)
        tipsRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        tipsRecyclerView.adapter = TipAdapter(tips)

        binding.bottomNavigationView.setSelectedItemId(R.id.bottom_home)
        binding.bottomNavigationView.setOnItemSelectedListener {
            if (it.itemId == R.id.bottom_home){
                true
            }else if (it.itemId == R.id.bottom_income){
                true
                val intent = Intent(this, Income::class.java)
                intent.putExtra("userEmail", userEmail)
                startActivity(intent)
                this.finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            }else if (it.itemId == R.id.bottom_expense){
                true
                val intent = Intent(this, Expenses::class.java)
                intent.putExtra("userEmail", userEmail)
                startActivity(intent)
                this.finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            }else if (it.itemId == R.id.bottom_profile){
                true
                val intent = Intent(this, Profile::class.java)
                intent.putExtra("userEmail", userEmail)
                startActivity(intent)
                this.finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            }

            true
        }

        val homeLogoutBtn = findViewById<ImageView>(R.id.home_logout_btn)
        homeLogoutBtn.setOnClickListener {
            userEmail = ""
            startActivity(Intent(this, Login::class.java))
            this.finish()
        }

    }

    private fun updateBalanceColor() {
        val balance = totalIncome - totalExpense
        val balanceView = binding.include2.homeBalance
        if (minGoal != null && balance < minGoal!!) {
            balanceView.setTextColor(Color.parseColor("#FF9800")) // Orange for below min
        } else if (maxGoal != null && balance > maxGoal!!) {
            balanceView.setTextColor(Color.parseColor("#F44336")) // Red for above max
        } else {
            balanceView.setTextColor(Color.parseColor("#FFFFFF")) // White/normal
        }
    }

    override fun onBackPressed(){
        startActivity(Intent(this, Login::class.java))
        this.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}