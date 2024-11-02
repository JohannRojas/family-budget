package com.example.familybudget.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.familybudget.fragments.ExpensesFragment
import com.example.familybudget.fragments.IncomesFragment
import com.example.familybudget.fragments.BudgetFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = listOf(
        BudgetFragment(),
        ExpensesFragment(),
        IncomesFragment()
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}