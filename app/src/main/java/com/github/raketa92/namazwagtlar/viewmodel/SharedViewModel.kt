package com.github.raketa92.namazwagtlar.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate

class SharedViewModel: ViewModel() {
    var selectedDate: MutableLiveData<LocalDate>? = null
}