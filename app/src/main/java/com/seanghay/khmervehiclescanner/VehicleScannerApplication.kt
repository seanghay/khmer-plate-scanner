package com.seanghay.khmervehiclescanner

import android.app.Application
import android.content.Context
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatDelegate

@Keep
class VehicleScannerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val sharedPref = getSharedPreferences("theme", Context.MODE_PRIVATE)
        val mode = sharedPref.getInt("mode", -1)
        if (mode != -1) {
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
}