package com.example.familybudget.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.familybudget.R
import com.example.familybudget.ui.CustomCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BudgetFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var totalBudgetCard: CustomCardView
    private lateinit var expensesCard: CustomCardView
    private lateinit var remainingBudgetCard: CustomCardView

    private var currentBudget: Double = 0.0
    private var currentExpenses: Double = 0.0

    private var budgetListener: ValueEventListener? = null
    private var expensesListener: ValueEventListener? = null

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

        configCards()

        val inputBudget = view.findViewById<TextInputEditText>(R.id.inputBudget)
        val addNewBudgetButton = view.findViewById<Button>(R.id.addNewBudget)

        addNewBudgetButton.setOnClickListener {
            val budget = inputBudget.text.toString().trim()
            if (budget.isNotEmpty()) {
                saveBudgetToFirebase(budget)
                inputBudget.text?.clear()
            } else {
                Log.d("BudgetFragment", "Budget input is empty")
                Toast.makeText(context, "Por favor, ingresa un presupuesto", Toast.LENGTH_SHORT).show()
            }
        }

        retrieveBudgetAndExpenses()

        return view
    }

    private fun configCards() {
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
    }

    private fun saveBudgetToFirebase(budget: String) {
        val budgetValue = budget.toDoubleOrNull()
        if (budgetValue == null || budgetValue < 0) {
            Toast.makeText(context, "Por favor, ingresa un presupuesto válido", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userBudgetRef = database.child("users").child(userId).child("budget")
            userBudgetRef.setValue(budgetValue).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Presupuesto guardado exitosamente", Toast.LENGTH_SHORT).show()
                    Log.d("BudgetFragment", "Budget saved successfully")
                } else {
                    Toast.makeText(context, "Error al guardar el presupuesto", Toast.LENGTH_SHORT).show()
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

            budgetListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentBudget = extractDouble(snapshot)
                    totalBudgetCard.setValueText(String.format("%.2f", currentBudget))
                    calculateRemainingBudget()
                }

                override fun onCancelled(error: DatabaseError) {
                    activity?.let {
                        Toast.makeText(it, "Error al recuperar el presupuesto", Toast.LENGTH_SHORT).show()
                        Log.e("BudgetFragment", "Failed to retrieve budget", error.toException())
                    }
                }
            }

            // Listener para los gastos
            expensesListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentExpenses = 0.0
                    for (expenseSnapshot in snapshot.children) {
                        val amount = extractDouble(expenseSnapshot.child("amount"))
                        currentExpenses += amount
                    }
                    expensesCard.setValueText(String.format("%.2f", currentExpenses))
                    calculateRemainingBudget()
                }

                override fun onCancelled(error: DatabaseError) {
                    activity?.let {
                        Toast.makeText(it, "Error al recuperar los gastos", Toast.LENGTH_SHORT).show()
                        Log.e("BudgetFragment", "Failed to retrieve expenses", error.toException())
                    }
                }
            }

            userBudgetRef.addValueEventListener(budgetListener!!)
            userExpensesRef.addValueEventListener(expensesListener!!)
        } else {
            Log.e("BudgetFragment", "User ID is null")
        }
    }

    private fun extractDouble(snapshot: DataSnapshot): Double {
        return when (val value = snapshot.getValue(Double::class.java)) {
            null -> {
                val strValue = snapshot.getValue(String::class.java)
                strValue?.toDoubleOrNull() ?: 0.0
            }
            else -> value
        }
    }

    private fun calculateRemainingBudget() {
        val remaining = currentBudget - currentExpenses
        remainingBudgetCard.setValueText(String.format("%.2f", remaining))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userBudgetRef = database.child("users").child(userId).child("budget")
            val userExpensesRef = database.child("users").child(userId).child("expenses")
            budgetListener?.let { userBudgetRef.removeEventListener(it) }
            expensesListener?.let { userExpensesRef.removeEventListener(it) }
        }
    }
}