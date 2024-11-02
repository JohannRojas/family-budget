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

class ExpensesFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var expensesContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_expenses, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        expensesContainer = view.findViewById(R.id.expensesContainer)

        retrieveExpensesFromFirebase()

        val inputOrigin = view.findViewById<TextInputEditText>(R.id.origin)
        val inputAmount = view.findViewById<TextInputEditText>(R.id.amount)
        val addExpensesButton = view.findViewById<Button>(R.id.addExpenses)

        addExpensesButton.setOnClickListener {
            val origin = inputOrigin.text.toString().trim()
            val amount = inputAmount.text.toString().trim()
            if (origin.isNotEmpty() && amount.isNotEmpty()) {
                saveExpenseToFirebase(origin, amount)
                inputAmount.text?.clear()
                inputOrigin.text?.clear()
            } else {
                Log.d("ExpensesFragment", "Origin or amount input is empty")
            }
        }

        return view
    }

    private fun saveExpenseToFirebase(origin: String, amount: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userExpensesRef = database.child("users").child(userId).child("expenses").push()
            val expense = mapOf("origin" to origin, "amount" to amount)
            userExpensesRef.setValue(expense).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Expense saved successfully", Toast.LENGTH_SHORT).show()
                    Log.d("ExpensesFragment", "Expense saved successfully")
                } else {
                    Toast.makeText(context, "Failed to save expense", Toast.LENGTH_SHORT).show()
                    Log.e("ExpensesFragment", "Failed to save expense", task.exception)
                }
            }
        } else {
            Log.e("ExpensesFragment", "User ID is null")
        }
    }

    private fun retrieveExpensesFromFirebase() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userExpensesRef = database.child("users").child(userId).child("expenses")
            userExpensesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    expensesContainer.removeAllViews()
                    for (expenseSnapshot in snapshot.children) {
                        val origin = expenseSnapshot.child("origin").getValue(String::class.java)
                        val amount = expenseSnapshot.child("amount").getValue(String::class.java)
                        if (origin != null && amount != null) {
                            val expenseCard = CustomCardView(requireContext())
                            expenseCard.setLabelText(origin)
                            expenseCard.setValueText(amount)
                            expenseCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.warning))

                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layoutParams.setMargins(16, 16, 16, 16)
                            expenseCard.layoutParams = layoutParams

                            expensesContainer.addView(expenseCard)
                        }
                    }
                    Log.d("ExpensesFragment", "Expenses retrieved")
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to retrieve expenses", Toast.LENGTH_SHORT).show()
                    Log.e("ExpensesFragment", "Failed to retrieve expenses", error.toException())
                }
            })
        } else {
            Log.e("ExpensesFragment", "User ID is null")
        }
    }
}