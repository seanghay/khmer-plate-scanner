package com.seanghay.khmervehiclescanner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    val showSheet = MutableLiveData(false)
    val scanStarted = MutableLiveData(false)
    val isLoading = MutableLiveData(false)
}