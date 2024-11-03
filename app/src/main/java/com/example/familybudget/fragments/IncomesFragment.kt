package com.example.familybudget.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.familybudget.R
import com.example.familybudget.ui.CustomCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class IncomesFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var incomesContainer: LinearLayout
    private lateinit var inputOrigin: TextInputEditText
    private lateinit var inputAmount: TextInputEditText
    private lateinit var addIncomesButton: Button
    private var editingIncomeId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_incomes, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        incomesContainer = view.findViewById(R.id.incomesContainer)
        inputOrigin = view.findViewById(R.id.origin)
        inputAmount = view.findViewById(R.id.amount)
        addIncomesButton = view.findViewById(R.id.addIncomes)

        retrieveIncomesFromFirebase()

        addIncomesButton.setOnClickListener {
            val origin = inputOrigin.text.toString().trim()
            val amount = inputAmount.text.toString().trim()
            if (origin.isNotEmpty() && amount.isNotEmpty()) {
                if (editingIncomeId != null) {
                    updateIncomeInFirebase(editingIncomeId!!, origin, amount)
                } else {
                    saveIncomeToFirebase(origin, amount)
                }
                inputAmount.text?.clear()
                inputOrigin.text?.clear()
                addIncomesButton.text = getString(R.string.add_income_button)
                editingIncomeId = null
            } else {
                Log.d("IncomesFragment", "Origin or amount input is empty")
            }
        }

        return view
    }

    private fun saveIncomeToFirebase(origin: String, amount: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userIncomesRef = database.child("users").child(userId).child("incomes").push()
            val income = mapOf("origin" to origin, "amount" to amount)
            userIncomesRef.setValue(income).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Income saved successfully", Toast.LENGTH_SHORT).show()
                    Log.d("IncomesFragment", "Income saved successfully")
                } else {
                    Toast.makeText(context, "Failed to save income", Toast.LENGTH_SHORT).show()
                    Log.e("IncomesFragment", "Failed to save income", task.exception)
                }
            }
        } else {
            Log.e("IncomesFragment", "User ID is null")
        }
    }

    private fun updateIncomeInFirebase(incomeId: String, origin: String, amount: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userIncomeRef = database.child("users").child(userId).child("incomes").child(incomeId)
            val updatedIncome = mapOf("origin" to origin, "amount" to amount)
            userIncomeRef.updateChildren(updatedIncome).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Income updated successfully", Toast.LENGTH_SHORT).show()
                    Log.d("IncomesFragment", "Income updated successfully")
                } else {
                    Toast.makeText(context, "Failed to update income", Toast.LENGTH_SHORT).show()
                    Log.e("IncomesFragment", "Failed to update income", task.exception)
                }
            }
        } else {
            Log.e("IncomesFragment", "User ID is null")
        }
    }

    private fun retrieveIncomesFromFirebase() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userIncomesRef = database.child("users").child(userId).child("incomes")
            userIncomesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    incomesContainer.removeAllViews()
                    for (incomeSnapshot in snapshot.children) {
                        val origin = incomeSnapshot.child("origin").getValue(String::class.java)
                        val amount = incomeSnapshot.child("amount").getValue(String::class.java)
                        val incomeId = incomeSnapshot.key
                        if (origin != null && amount != null && incomeId != null) {
                            val incomeCard = CustomCardView(requireContext())
                            incomeCard.setLabelText(origin)
                            incomeCard.setValueText(amount)
                            incomeCard.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                            incomeCard.setLabelTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                            incomeCard.showOptionsButton(true)
                            incomeCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))


                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layoutParams.setMargins(32, 0, 32, 16)
                            incomeCard.layoutParams = layoutParams

                            incomeCard.setOnEditClickListener {
                                // Handle edit action
                                inputOrigin.setText(origin)
                                inputAmount.setText(amount)
                                addIncomesButton.text = getString(R.string.edit_income_button)
                                editingIncomeId = incomeId
                            }

                            incomeCard.setOnDeleteClickListener {
                                // Handle delete action
                                deleteIncome(incomeId)
                            }

                            incomesContainer.addView(incomeCard)
                        }
                    }
                    Log.d("IncomesFragment", "Incomes retrieved")
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to retrieve incomes", Toast.LENGTH_SHORT).show()
                    Log.e("IncomesFragment", "Failed to retrieve incomes", error.toException())
                }
            })
        } else {
            Log.e("IncomesFragment", "User ID is null")
        }
    }

    private fun deleteIncome(incomeId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userIncomeRef = database.child("users").child(userId).child("incomes").child(incomeId)
            userIncomeRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Income deleted successfully", Toast.LENGTH_SHORT).show()
                    Log.d("IncomesFragment", "Income deleted successfully")
                } else {
                    Toast.makeText(context, "Failed to delete income", Toast.LENGTH_SHORT).show()
                    Log.e("IncomesFragment", "Failed to delete income", task.exception)
                }
            }
        } else {
            Log.e("IncomesFragment", "User ID is null")
        }
    }
}