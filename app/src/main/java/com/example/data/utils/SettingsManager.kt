package com.example.data.utils

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("vidnexa_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_AUTO_DELETE = "auto_delete"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_ANALYTICS = "analytics"
        private const val KEY_DEMO_MODE = "demo_mode"
    }

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, "https://vidnexa.space") ?: "https://vidnexa.space"
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()

    var useDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true) // Default to true (premium dark theme!)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var autoDeleteTemp: Boolean
        get() = prefs.getBoolean(KEY_AUTO_DELETE, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_DELETE, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    var analyticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_ANALYTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_ANALYTICS, value).apply()

    var demoMode: Boolean
        get() = prefs.getBoolean(KEY_DEMO_MODE, false) // Default to false, can toggle for demonstration
        set(value) = prefs.edit().putBoolean(KEY_DEMO_MODE, value).apply()
}
