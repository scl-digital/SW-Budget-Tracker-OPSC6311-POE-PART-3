package com.groupproject.spendwise

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.groupproject.spendwise.databinding.ActivityExpenseAddNewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class expense_add_new : AppCompatActivity() {
    private lateinit var binding: ActivityExpenseAddNewBinding
    private var selectedImageUri: Uri? = null
    private lateinit var progressBar: ProgressBar
    private var selectedExpenseType: String = ""
    private var selectedIcon: Int = 0
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseAddNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userEmail = intent.getStringExtra("userEmail") ?: firebaseAuth.currentUser?.email ?: ""
        val userKey = userEmail.replace(Regex("""[.#$\[\]]"""), "_")

        val expenseTypes = listOf("Food", "Transportation", "Bills", "Education", "Health", "Other")
        val autocomplete: AutoCompleteTextView = binding.expenseDropItems

        setupDatePicker()
        setupExpenseTypeDropdown(expenseTypes, autocomplete)
        setupPhotoSelection()
        setupBackButton(userEmail)
        setupSaveButton(userKey, userEmail)
    }

    private fun setupDatePicker() {
        val cal = Calendar.getInstance()
        binding.expenseDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                binding.expenseDate.setText("$year/${month + 1}/$day")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupExpenseTypeDropdown(expenseTypes: List<String>, autocomplete: AutoCompleteTextView) {
        val adapter = ArrayAdapter(this, R.layout.list_items_income, expenseTypes)
        autocomplete.setAdapter(adapter)
        autocomplete.onItemClickListener = AdapterView.OnItemClickListener { adapterView, _, i, _ ->
            val itemSelected = adapterView.getItemAtPosition(i).toString()
            selectedExpenseType = itemSelected
            selectedIcon = when (itemSelected) {
                "Food" -> R.drawable.food
                "Transportation" -> R.drawable.transportation
                "Bills" -> R.drawable.bills
                "Education" -> R.drawable.education
                "Health" -> R.drawable.health
                "Other" -> R.drawable.other_expenses
                else -> R.drawable.expense_pic
            }
        }
    }

    private fun setupPhotoSelection() {
        progressBar = binding.expenseProgressBar
        binding.expenseAddPhotoBtn.setOnClickListener {
            pickImageFromGallery()
        }
    }

    private fun setupBackButton(userEmail: String) {
        // No addExpensesBack in layout, so skip or remove this
        // binding.addExpensesBack.setOnClickListener {
        //     navigateToExpenses(userEmail)
        // }
    }

    private fun setupSaveButton(userKey: String, userEmail: String) {
        binding.expenseSaveBtn.setOnClickListener {
            val amount = binding.expenseAmount.text?.toString()?.trim() ?: ""
            val date = binding.expenseDate.text?.toString()?.trim() ?: ""
            val description = binding.expenseAddDescription.text?.toString()?.trim() ?: ""

            if (validateInput(amount, date, selectedExpenseType, description)) {
                progressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.Main).launch {
                    saveExpenseToFirebase(userKey, amount, date, selectedExpenseType, selectedIcon, description, userEmail)
                }
            }
        }
    }

    private fun validateInput(amount: String, date: String, type: String, description: String): Boolean {
        if (amount.isEmpty() || date.isEmpty() || type.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private suspend fun saveExpenseToFirebase(
        userKey: String,
        amount: String,
        date: String,
        type: String,
        icon: Int,
        description: String,
        userEmail: String
    ) {
        try {
            val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userKey).child("expense")
            var photoPath: String? = null
            if (selectedImageUri != null) {
                val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val fileName = "expense_${System.currentTimeMillis()}.png"
                val file = File(filesDir, fileName)
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()
                photoPath = file.absolutePath
            }
            val expenseValue = mapOf(
                "amount" to amount,
                "date" to date,
                "type" to type,
                "icon" to icon,
                "description" to description,
                "photoPath" to photoPath,
                "createdAt" to System.currentTimeMillis()
            )
            dbRef.push().setValue(expenseValue).addOnCompleteListener {
                progressBar.visibility = View.GONE
                if (it.isSuccessful) {
                    Toast.makeText(this, "Expense Added Successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            binding.expensePhotoPreview.apply {
                setImageURI(selectedImageUri)
                visibility = View.VISIBLE
            }
        }
    }

    private fun navigateToExpenses(userEmail: String) {
        val intent = Intent(this, Expenses::class.java)
        intent.putExtra("userEmail", userEmail)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }
}