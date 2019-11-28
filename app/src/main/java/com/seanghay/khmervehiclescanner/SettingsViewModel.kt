package com.seanghay.khmervehiclescanner

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class SettingsViewModel : ViewModel() {

    val locale = MutableLiveData(Locale.getDefault().language)

    val currentTheme = MutableLiveData<Int>(AppCompatDelegate.getDefaultNightMode())
    val currentCheckedTheme = Transformations.map(currentTheme) {
        when (it) {
            AppCompatDelegate.MODE_NIGHT_YES -> R.id.chipDarkTheme
            AppCompatDelegate.MODE_NIGHT_NO -> R.id.chipLightTheme
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> R.id.chipSystemTheme
            else -> R.id.chipSystemTheme
        }
    }

}
