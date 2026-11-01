package dev.powerx.browser

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import dev.powerx.browser.settings.AppSettings

class SettingsActivity : AppCompatActivity() {

    private lateinit var settings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = AppSettings(this)
        setContentView(R.layout.activity_settings)

        val homepage = findViewById<EditText>(R.id.homepage)
        val searchEngine = findViewById<Spinner>(R.id.searchEngine)
        val theme = findViewById<Spinner>(R.id.theme)

        homepage.setText(settings.homepage)

        val engineNames = AppSettings.ENGINES.map { it.name }
        searchEngine.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, engineNames)
        val currentEngine = AppSettings.ENGINES.indexOfFirst { it.template == settings.searchTemplate }
        searchEngine.setSelection(if (currentEngine >= 0) currentEngine else 0)

        val themeNames = listOf(
            getString(R.string.theme_system),
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        )
        val themeValues = listOf(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES
        )
        theme.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themeNames)
        theme.setSelection(themeValues.indexOf(settings.themeMode).coerceAtLeast(0))

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val hp = homepage.text.toString().trim()
            settings.homepage = if (hp.isEmpty()) AppSettings.DEFAULT_HOMEPAGE else hp
            settings.searchTemplate = AppSettings.ENGINES[searchEngine.selectedItemPosition].template
            settings.themeMode = themeValues[theme.selectedItemPosition]
            settings.applyTheme()
            finish()
        }
    }
}
