package com.example.familybudget.ui.bills

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BillsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Budget Fragment"
    }
    val text: LiveData<String> = _text

}