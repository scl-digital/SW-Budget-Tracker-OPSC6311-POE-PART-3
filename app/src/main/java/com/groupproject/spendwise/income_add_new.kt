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
import com.groupproject.spendwise.databinding.ActivityIncomeAddNewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class income_add_new : AppCompatActivity() {
    private lateinit var binding: ActivityIncomeAddNewBinding
    private var selectedImageUri: Uri? = null
    private lateinit var progressBar: ProgressBar
    private var selectedIncomeType: String = ""
    private var selectedIcon: Int = 0
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomeAddNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userEmail = intent.getStringExtra("userEmail") ?: firebaseAuth.currentUser?.email ?: ""
        val userKey = userEmail.replace(Regex("""[.#$\[\]]"""), "_")

        val incomeTypes = listOf("Salary", "Rental", "Investments", "Other")
        val autocomplete: AutoCompleteTextView = binding.dropItems

        setupDatePicker()
        setupIncomeTypeDropdown(incomeTypes, autocomplete)
        setupPhotoSelection()
        setupBackButton(userEmail)
        setupSaveButton(userKey, userEmail)
    }

    private fun setupDatePicker() {
        val cal = Calendar.getInstance()
        binding.incomeDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                binding.incomeDate.setText("$year/${month + 1}/$day")
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupIncomeTypeDropdown(incomeTypes: List<String>, autocomplete: AutoCompleteTextView) {
        val adapter = ArrayAdapter(this, R.layout.list_items_income, incomeTypes)
        autocomplete.setAdapter(adapter)
        autocomplete.onItemClickListener = AdapterView.OnItemClickListener { adapterView, _, i, _ ->
            val itemSelected = adapterView.getItemAtPosition(i).toString()
            selectedIncomeType = itemSelected
            selectedIcon = when (itemSelected) {
                "Salary" -> R.drawable.salary
                "Rental" -> R.drawable.rental
                "Investments" -> R.drawable.investments
                "Other" -> R.drawable.other_income
                else -> R.drawable.income_pic
            }
        }
    }

    private fun setupPhotoSelection() {
        progressBar = binding.incomeProgressBar
        binding.incomeAddPhotoBtn.setOnClickListener {
            pickImageFromGallery()
        }
    }

    private fun setupBackButton(userEmail: String) {
        binding.addIncomeBacK.setOnClickListener {
            navigateToIncome(userEmail)
        }
    }

    private fun setupSaveButton(userKey: String, userEmail: String) {
        binding.incomeSaveBtn.setOnClickListener {
            val amount = binding.incomeAmount.text?.toString()?.trim() ?: ""
            val date = binding.incomeDate.text?.toString()?.trim() ?: ""
            val description = binding.incomeAddDescription.text?.toString()?.trim() ?: ""

            if (validateInput(amount, date, selectedIncomeType, description)) {
                progressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.Main).launch {
                    saveIncomeToFirebase(userKey, amount, date, selectedIncomeType, selectedIcon, description, userEmail)
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

    private suspend fun saveIncomeToFirebase(
        userKey: String,
        amount: String,
        date: String,
        type: String,
        icon: Int,
        description: String,
        userEmail: String
    ) {
        try {
            val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userKey).child("income")
            var photoPath: String? = null
            if (selectedImageUri != null) {
                val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val fileName = "income_${System.currentTimeMillis()}.png"
                val file = File(filesDir, fileName)
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()
                photoPath = file.absolutePath
            }
            val incomeValue = mapOf(
                "amount" to amount,
                "date" to date,
                "type" to type,
                "icon" to icon,
                "description" to description,
                "photoPath" to photoPath,
                "createdAt" to System.currentTimeMillis()
            )
            dbRef.push().setValue(incomeValue).addOnCompleteListener {
                progressBar.visibility = View.GONE
                if (it.isSuccessful) {
                    Toast.makeText(this, "Income Added Successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    navigateToIncome(userEmail)
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
            binding.incomePhotoPreview.apply {
                setImageURI(selectedImageUri)
                visibility = View.VISIBLE
            }
        }
    }

    private fun navigateToIncome(userEmail: String) {
        val intent = Intent(this, Income::class.java)
        intent.putExtra("userEmail", userEmail)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }
}