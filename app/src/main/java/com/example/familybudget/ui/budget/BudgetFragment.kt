package com.example.familybudget.ui.budget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.familybudget.databinding.FragmentBillsBinding

class BudgetFragment : Fragment() {

    private var _binding: FragmentBillsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val budgetViewModel =
            ViewModelProvider(this).get(BudgetViewModel::class.java)

        _binding = FragmentBillsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textBills
        budgetViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}