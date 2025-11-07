package com.example.chiligif.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import java.util.Locale

object LocaleHelper {
    private const val PREF_LANGUAGE = "app_language"
    private const val LATVIAN = "lv"
    private const val ENGLISH = "en"

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        // Save preference
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit {
                putString(PREF_LANGUAGE, languageCode)
            }

        return context.createConfigurationContext(config)
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANGUAGE, ENGLISH) ?: ENGLISH
    }

    fun toggleLanguage(context: Context): String {
        val currentLang = getSavedLanguage(context)
        return if (currentLang == ENGLISH) LATVIAN else ENGLISH
    }

    fun recreateActivity(activity: Activity, languageCode: String) {
        setLocale(activity, languageCode)
        activity.recreate()
    }
}

