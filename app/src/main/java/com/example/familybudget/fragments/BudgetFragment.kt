package com.example.familybudget.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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

class BudgetFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var totalBudgetCard: CustomCardView
    private lateinit var expensesCard: CustomCardView
    private lateinit var remainingBudgetCard: CustomCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_budget, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        totalBudgetCard = view.findViewById(R.id.totalBudgetCard)
        expensesCard = view.findViewById(R.id.billsCard)
        remainingBudgetCard = view.findViewById(R.id.remainingCard)

        totalBudgetCard.setLabelText("Presupuesto total")
        totalBudgetCard.setLabelTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        totalBudgetCard.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        totalBudgetCard.showOptionsButton(false)

        expensesCard.setLabelText("Gastos")
        expensesCard.setLabelTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        expensesCard.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        expensesCard.showOptionsButton(false)

        remainingBudgetCard.setLabelText("Presupuesto restante")
        remainingBudgetCard.setLabelTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        remainingBudgetCard.setValueTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        remainingBudgetCard.showOptionsButton(false)

        retrieveBudgetAndExpenses()

        val inputBudget = view.findViewById<TextInputEditText>(R.id.inputBudget)
        val addNewBudgetButton = view.findViewById<Button>(R.id.addNewBudget)

        addNewBudgetButton.setOnClickListener {
            val budget = inputBudget.text.toString().trim()
            if (budget.isNotEmpty()) {
                saveBudgetToFirebase(budget)
            } else {
                Log.d("BudgetFragment", "Budget input is empty")
            }
        }

        return view
    }

    private fun saveBudgetToFirebase(budget: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userBudgetRef = database.child("users").child(userId).child("budget")
            userBudgetRef.setValue(budget).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Budget saved successfully", Toast.LENGTH_SHORT).show()
                    Log.d("BudgetFragment", "Budget saved successfully")
                } else {
                    Toast.makeText(context, "Failed to save budget", Toast.LENGTH_SHORT).show()
                    Log.e("BudgetFragment", "Failed to save budget", task.exception)
                }
            }
        } else {
            Log.e("BudgetFragment", "User ID is null")
        }
    }

    private fun retrieveBudgetAndExpenses() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userBudgetRef = database.child("users").child(userId).child("budget")
            val userExpensesRef = database.child("users").child(userId).child("expenses")

            userBudgetRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val budget = snapshot.getValue(String::class.java)?.toIntOrNull() ?: 0
                    totalBudgetCard.setValueText(budget.toString())
                    calculateRemainingBudget(budget)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to retrieve budget", Toast.LENGTH_SHORT).show()
                    Log.e("BudgetFragment", "Failed to retrieve budget", error.toException())
                }
            })

            userExpensesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalExpenses = 0
                    for (expenseSnapshot in snapshot.children) {
                        val amount = expenseSnapshot.child("amount").getValue(String::class.java)
                            ?.toIntOrNull() ?: 0
                        totalExpenses += amount
                    }
                    expensesCard.setValueText(totalExpenses.toString())
                    calculateRemainingBudget(null, totalExpenses)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to retrieve expenses", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("BudgetFragment", "Failed to retrieve expenses", error.toException())
                }
            })
        } else {
            Log.e("BudgetFragment", "User ID is null")
        }
    }

    private fun calculateRemainingBudget(budget: Int? = null, expenses: Int? = null) {
        val currentBudget = budget ?: totalBudgetCard.getValueText().toIntOrNull() ?: 0
        val currentExpenses = expenses ?: expensesCard.getValueText().toIntOrNull() ?: 0
        val remaining = currentBudget - currentExpenses
        remainingBudgetCard.setValueText(remaining.toString())
    }
}