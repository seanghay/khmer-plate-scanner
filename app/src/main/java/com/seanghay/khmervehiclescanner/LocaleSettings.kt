package com.seanghay.khmervehiclescanner

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import java.util.*


class LocaleSettings constructor(private val context: Context) {

    val localePref = PreferenceManager.getDefaultSharedPreferences(context)

    val storedLocale: Locale =
        localePref.getString("app-locale", null)?.run { Locale.forLanguageTag(this) }
            ?: Locale.getDefault()

    fun applyLocale(languageTag: String) {
        localePref.edit()
            .putString("app-locale", languageTag)
            .apply()
    }

    fun followSystem() {
        localePref.edit().remove("app-locale").apply()
    }

    fun attachLocale(languageTag: String): Context {
        return attachLocale(Locale.forLanguageTag(languageTag))
    }


    fun attachLocale(locale: Locale): Context {
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        Locale.setDefault(locale)
        return context.createConfigurationContext(config)
    }

    companion object {

        fun isFollowSystem(context: Context): Boolean {
            return LocaleSettings(context).run {
                !localePref.contains("app-locale")
            }
        }

        fun followSystem(context: Context) {

            return LocaleSettings(context).run {
                followSystem()
            }
        }

        fun setDefaultLanguage(fragment: Fragment, languageTag: String) {
            setDefaultLanguage(fragment.activity!!, languageTag)
        }

        fun setDefaultLanguage(activity: Activity, languageTag: String) {

            return LocaleSettings(activity).run {
                val willRecreate = storedLocale.toLanguageTag() != languageTag
                applyLocale(languageTag)
                attachLocale(storedLocale)

                if (willRecreate)
                    activity.recreate()
            }
        }

        fun attach(oldContext: Context): Context {
            return LocaleSettings(oldContext).run {
                attachLocale(storedLocale)
            }
        }
    }

}
