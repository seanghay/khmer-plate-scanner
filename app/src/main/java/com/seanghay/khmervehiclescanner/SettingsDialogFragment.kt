package com.seanghay.khmervehiclescanner

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.dialog_fragment_settings.*
import java.util.*

class SettingsDialogFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var sharedPreference: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreference = context.getSharedPreferences("theme", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_fragment_settings, container, false)
    }

    private fun setThemeMode(mode: Int) {
        sharedPreference.edit().putInt("mode", mode).apply()
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun setLocale(id: Int, tag: String) {
        LocaleSettings.setDefaultLanguage(this, tag)
        chipGroupLanguages.clearCheck()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initLocale()

        viewModel.currentCheckedTheme.observe(viewLifecycleOwner, Observer {
            chipGroupThemes.clearCheck()
            chipGroupThemes.check(it)
        })

        chipLightTheme.setOnClickListener {
            setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)
            viewModel.currentTheme.value = AppCompatDelegate.MODE_NIGHT_NO
        }

        chipDarkTheme.setOnClickListener {
            setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)
            viewModel.currentTheme.value = AppCompatDelegate.MODE_NIGHT_YES
        }

        chipSystemTheme.setOnClickListener {
            setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            viewModel.currentTheme.value = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        chipKmLocale.setOnClickListener {
            setLocale(it.id, "km")
            chipGroupLanguages.check(it.id)
        }

        chipEnLocale.setOnClickListener {
            setLocale(it.id, "en-US")
            chipGroupLanguages.check(it.id)
        }

        chipSystemLocale.setOnClickListener {
            val locales =
                ConfigurationCompat.getLocales(requireActivity().application.resources.configuration)
            if (!locales.isEmpty) {
                LocaleSettings.followSystem(requireContext())
                setLocale(it.id, locales.get(0).toLanguageTag())
            }
            chipGroupLanguages.check(it.id)
        }
    }

    private fun initLocale() {
        val localeSettings = LocaleSettings(requireContext())
        val tag = localeSettings.localePref.getString("app-locale", null)

        if (tag == null) {
            chipGroupLanguages.check(R.id.chipSystemLocale)
        } else {
            val locale = Locale.forLanguageTag(tag)
            when (locale.language) {
                "km" -> {
                    chipGroupLanguages.check(R.id.chipKmLocale)
                }
                "en" -> {
                    chipGroupLanguages.check(R.id.chipEnLocale)
                }
                else -> {
                    chipGroupLanguages.check(R.id.chipSystemLocale)
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}
