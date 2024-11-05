package com.example.familybudget.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.familybudget.R
import com.example.familybudget.ui.CustomCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ExpensesFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var expensesContainer: LinearLayout
    private lateinit var inputOrigin: TextInputEditText
    private lateinit var inputAmount: TextInputEditText
    private lateinit var addExpensesButton: Button
    private lateinit var noExpensesMessage: TextView
    private var editingExpenseId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expenses, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        expensesContainer = view.findViewById(R.id.expensesContainer)
        inputOrigin = view.findViewById(R.id.origin)
        inputAmount = view.findViewById(R.id.amount)
        addExpensesButton = view.findViewById(R.id.addExpenses)
        noExpensesMessage = view.findViewById(R.id.noExpensesMessage)

        retrieveExpensesFromFirebase()

        addExpensesButton.setOnClickListener {
            val origin = inputOrigin.text.toString().trim()
            val amountStr = inputAmount.text.toString().trim()
            if (origin.isNotEmpty() && amountStr.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount >= 0.0) {
                    if (editingExpenseId != null) {
                        updateExpenseInFirebase(editingExpenseId!!, origin, amount)
                    } else {
                        saveExpenseToFirebase(origin, amount)
                    }
                    inputAmount.text?.clear()
                    inputOrigin.text?.clear()
                    addExpensesButton.text = getString(R.string.add_expense_button)
                    editingExpenseId = null
                } else {
                    Toast.makeText(context, "Por favor, ingresa un monto válido", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("ExpensesFragment", "Origin or amount input is empty")
                Toast.makeText(context, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun saveExpenseToFirebase(origin: String, amount: Double) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userExpensesRef = database.child("users").child(userId).child("expenses").push()
            val expense = mapOf(
                "origin" to origin,
                "amount" to amount
            )
            userExpensesRef.setValue(expense).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Gasto guardado exitosamente", Toast.LENGTH_SHORT).show()
                    Log.d("ExpensesFragment", "Expense saved successfully")
                } else {
                    Toast.makeText(context, "Error al guardar el gasto", Toast.LENGTH_SHORT).show()
                    Log.e("ExpensesFragment", "Failed to save expense", task.exception)
                }
            }
        } else {
            Log.e("ExpensesFragment", "User ID is null")
        }
    }

    private fun updateExpenseInFirebase(expenseId: String, origin: String, amount: Double) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userExpenseRef = database.child("users").child(userId).child("expenses").child(expenseId)
            val updatedExpense = mapOf(
                "origin" to origin,
                "amount" to amount
            )
            userExpenseRef.updateChildren(updatedExpense).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Gasto actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    Log.d("ExpensesFragment", "Expense updated successfully")
                } else {
                    Toast.makeText(context, "Error al actualizar el gasto", Toast.LENGTH_SHORT).show()
                    Log.e("ExpensesFragment", "Failed to update expense", task.exception)
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

                    if (!snapshot.exists() || !snapshot.hasChildren()) {
                        noExpensesMessage.visibility = View.VISIBLE
                        expensesContainer.visibility = View.GONE
                    } else {
                        noExpensesMessage.visibility = View.GONE
                        expensesContainer.visibility = View.VISIBLE

                        for (expenseSnapshot in snapshot.children) {
                            val origin = expenseSnapshot.child("origin").getValue(String::class.java)
                            val amount = extractDouble(expenseSnapshot.child("amount"))
                            val expenseId = expenseSnapshot.key
                            if (origin != null && expenseId != null) {
                                val expenseCard = CustomCardView(requireContext())
                                expenseCard.setLabelText(origin)
                                expenseCard.setValueText(String.format("%.2f", amount))
                                expenseCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.warning))
                                expenseCard.showOptionsButton(true)

                                val layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                layoutParams.setMargins(32, 0, 32, 16)
                                expenseCard.layoutParams = layoutParams

                                expenseCard.setOnEditClickListener {
                                    inputOrigin.setText(origin)
                                    inputAmount.setText(String.format("%.2f", amount))
                                    addExpensesButton.text = getString(R.string.edit_expense_button)
                                    editingExpenseId = expenseId
                                }

                                expenseCard.setOnDeleteClickListener {
                                    deleteExpense(expenseId)
                                }

                                expensesContainer.addView(expenseCard)
                            }
                        }
                    }
                    Log.d("ExpensesFragment", "Expenses retrieved")
                }

                override fun onCancelled(error: DatabaseError) {
                    context?.let { ctx ->
                        Toast.makeText(ctx, "Error al recuperar los gastos", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("ExpensesFragment", "Failed to retrieve expenses", error.toException())
                }
            })
        } else {
            Log.e("ExpensesFragment", "User ID is null")
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

    private fun deleteExpense(expenseId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userExpenseRef = database.child("users").child(userId).child("expenses").child(expenseId)
            userExpenseRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Gasto eliminado exitosamente", Toast.LENGTH_SHORT).show()
                    Log.d("ExpensesFragment", "Expense deleted successfully")
                } else {
                    Toast.makeText(context, "Error al eliminar el gasto", Toast.LENGTH_SHORT).show()
                    Log.e("ExpensesFragment", "Failed to delete expense", task.exception)
                }
            }
        } else {
            Log.e("ExpensesFragment", "User ID is null")
        }
    }
}