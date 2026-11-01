package dev.powerx.browser

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import dev.powerx.browser.engine.BrowserEngine
import dev.powerx.browser.engine.gecko.GeckoBrowserEngine
import dev.powerx.browser.settings.AppSettings
import dev.powerx.browser.storage.BrowsingHistory
import java.net.URLEncoder

/** Single-tab browser shell. Engine contact goes through [BrowserEngine] only. */
class MainActivity : AppCompatActivity() {

    private lateinit var engine: BrowserEngine
    private lateinit var settings: AppSettings
    private lateinit var history: BrowsingHistory
    private lateinit var addressBar: EditText
    private lateinit var progress: ProgressBar
    private var lastLocation: String? = null
    private var lastTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {        super.onCreate(savedInstanceState)
        settings = AppSettings(this)
        settings.applyTheme()
        history = BrowsingHistory(this)
        setContentView(R.layout.activity_main)

        addressBar = findViewById(R.id.addressBar)
        progress = findViewById(R.id.progress)
        val host = findViewById<FrameLayout>(R.id.engineHost)

        // Engine slot: swap GeckoBrowserEngine for a Chromium module later
        // without touching anything below this line except construction.
        engine = GeckoBrowserEngine(this)
        engine.setListener(object : BrowserEngine.EngineListener {
            override fun onProgress(progress: Int) {
                this@MainActivity.progress.visibility = if (progress in 1..99) View.VISIBLE else View.GONE
                this@MainActivity.progress.progress = progress
            }

            override fun onLocation(url: String?) {
                lastLocation = url
                if (url != null) {
                    addressBar.setText(url)
                    history.record(url, lastTitle)
                }
            }

            override fun onTitle(title: String?) {
                lastTitle = title
            }

            override fun onNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
                findViewById<ImageButton>(R.id.btnBack).isEnabled = canGoBack
                findViewById<ImageButton>(R.id.btnForward).isEnabled = canGoForward
            }
        })
        engine.attach(host)

        findViewById<Button>(R.id.btnGo).setOnClickListener { navigate(addressBar.text.toString()) }
        addressBar.setOnEditorActionListener { _, _, _ ->
            navigate(addressBar.text.toString())
            true
        }
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { engine.goBack() }
        findViewById<ImageButton>(R.id.btnForward).setOnClickListener { engine.goForward() }
        findViewById<ImageButton>(R.id.btnReload).setOnClickListener { engine.reload() }
        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener { showMenu() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!engine.goBack()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        val initial = intent?.dataString ?: settings.homepage
        navigate(initial)
    }

    private fun navigate(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return
        engine.loadUrl(toUrl(trimmed))
    }

    private fun toUrl(input: String): String {
        if (input.startsWith("http://") || input.startsWith("https://")) return input
        if (input.contains(" ") || !input.contains(".")) {
            val q = URLEncoder.encode(input, "UTF-8")
            return settings.searchTemplate.replace("%s", q)
        }
        return "https://$input"
    }

    private fun showMenu() {
        val options = arrayOf(
            getString(R.string.extensions),
            getString(R.string.settings)
        )
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startActivity(Intent(this, ExtensionsActivity::class.java))
                    1 -> startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
            .show()
    }

    override fun onDestroy() {
        engine.setListener(null)
        engine.destroy()
        super.onDestroy()
    }
}
