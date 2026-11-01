package dev.powerx.browser.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/** Typed settings store. Original implementation; keys are ours. */
class AppSettings(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var homepage: String
        get() = prefs.getString(KEY_HOMEPAGE, DEFAULT_HOMEPAGE) ?: DEFAULT_HOMEPAGE
        set(value) = prefs.edit().putString(KEY_HOMEPAGE, value).apply()

    var searchTemplate: String
        get() = prefs.getString(KEY_SEARCH, ENGINES[0].template) ?: ENGINES[0].template
        set(value) = prefs.edit().putString(KEY_SEARCH, value).apply()

    var themeMode: Int
        get() = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) = prefs.edit().putInt(KEY_THEME, value).apply()

    fun applyTheme() {
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    data class SearchEngine(val name: String, val template: String)

    companion object {
        private const val PREFS = "powerx_settings"
        private const val KEY_HOMEPAGE = "homepage"
        private const val KEY_SEARCH = "search_template"
        private const val KEY_THEME = "theme_mode"
        const val DEFAULT_HOMEPAGE = "https://duckduckgo.com/"

        val ENGINES = listOf(
            SearchEngine("DuckDuckGo", "https://duckduckgo.com/?q=%s"),
            SearchEngine("Brave", "https://search.brave.com/search?q=%s"),
            SearchEngine("Google", "https://www.google.com/search?q=%s"),
            SearchEngine("Bing", "https://www.bing.com/search?q=%s"),
            SearchEngine("Ecosia", "https://www.ecosia.org/search?q=%s")
        )
    }
}
