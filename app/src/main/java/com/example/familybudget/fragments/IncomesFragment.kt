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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_incomes, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        incomesContainer = view.findViewById(R.id.incomesContainer)

        retrieveIncomesFromFirebase()

        val inputOrigin = view.findViewById<TextInputEditText>(R.id.origin)
        val inputAmount = view.findViewById<TextInputEditText>(R.id.amount)
        val addIncomesButton = view.findViewById<Button>(R.id.addIncomes)

        addIncomesButton.setOnClickListener {
            val origin = inputOrigin.text.toString().trim()
            val amount = inputAmount.text.toString().trim()
            if (origin.isNotEmpty() && amount.isNotEmpty()) {
                saveIncomeToFirebase(origin, amount)
                inputAmount.text?.clear()
                inputOrigin.text?.clear()
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
                        if (origin != null && amount != null) {
                            val incomeCard = CustomCardView(requireContext())
                            incomeCard.setLabelText(origin)
                            incomeCard.setValueText(amount)
                            incomeCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondary))

                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layoutParams.setMargins(16, 16, 16, 16)
                            incomeCard.layoutParams = layoutParams

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
}