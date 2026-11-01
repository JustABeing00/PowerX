package dev.powerx.browser

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dev.powerx.browser.engine.gecko.EngineProvider
import dev.powerx.browser.engine.gecko.GeckoBrowserEngine
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtensionController

/**
 * Honest extension manager for the Gecko proof-of-concept engine:
 * install-by-URL, list, uninstall. Chrome Web Store is reported
 * unsupported here (see capability label) — never faked.
 */
class ExtensionsActivity : AppCompatActivity() {

    private var engine: GeckoBrowserEngine? = null
    private val shown = mutableListOf<WebExtension>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extensions)

        status = findViewById(R.id.status)
        val urlInput = findViewById<EditText>(R.id.extensionUrl)
        val list = findViewById<ListView>(R.id.extensionList)

        engine = EngineProvider.require()
        engine?.let {
            status.text = it.extensionCapabilities.userFacingLabel
        }
        // Permission gate (E03): every install/update shows origin + permissions
        // and completes only on explicit Allow. No prompt = install stalls.
        engine?.setPromptDelegate(object : WebExtensionController.PromptDelegate {
            override fun onInstallPromptRequest(
                extension: WebExtension,
                permissions: Array<String>,
                origins: Array<String>,
                dataCollectionPermissions: Array<String>
            ): GeckoResult<WebExtension.PermissionPromptResponse> {
                val result = GeckoResult<WebExtension.PermissionPromptResponse>()
                runOnUiThread {
                    val lines = buildList {
                        add("Extension: ${extension.id}")
                        if (permissions.isNotEmpty()) {
                            add("Permissions:")
                            permissions.forEach { add("• $it") }
                        }
                        if (origins.isNotEmpty()) {
                            add("Site access:")
                            origins.forEach { add("• $it") }
                        }
                        if (dataCollectionPermissions.isNotEmpty()) {
                            add("Data collection:")
                            dataCollectionPermissions.forEach { add("• $it") }
                        }
                    }
                    AlertDialog.Builder(this@ExtensionsActivity)
                        .setTitle("Install extension?")
                        .setMessage(lines.joinToString("\n"))
                        .setPositiveButton("Allow") { _, _ ->
                            result.complete(
                                WebExtension.PermissionPromptResponse(true, false, false)
                            )
                        }
                        .setNegativeButton("Deny") { _, _ ->
                            result.complete(
                                WebExtension.PermissionPromptResponse(false, false, false)
                            )
                        }
                        .setOnCancelListener {
                            result.complete(
                                WebExtension.PermissionPromptResponse(false, false, false)
                            )
                        }
                        .show()
                }
                return result
            }
        })

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        list.adapter = adapter
        list.setOnItemClickListener { _, _, position, _ ->
            if (position in shown.indices) confirmUninstall(shown[position])
        }

        findViewById<Button>(R.id.btnInstall).setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isEmpty()) {
                status.text = "Enter an extension URL first."
                return@setOnClickListener
            }
            status.text = "Installing…"
            consume(engine?.installExtension(url), "Install") {
                status.text = if (it != null) "Installed: ${it.id}" else "Installed."
                refresh()
            }
        }

        refresh()
    }

    /**
     * Bridges GeckoResult with fully explicit SAM types so no generic
     * inference is needed at the call sites.
     */
    private fun <T> consume(result: GeckoResult<T>?, op: String, onOk: (T?) -> Unit) {
        if (result == null) {
            status.text = "$op failed: engine unavailable."
            return
        }
        result.then(
            GeckoResult.OnValueListener<T, Void?> { value ->
                runOnUiThread { onOk(value) }
                null
            },
            GeckoResult.OnExceptionListener<Void?> { e ->
                runOnUiThread {
                    status.text = "$op failed: ${e?.message ?: "unknown error"}"
                }
                null
            }
        )
    }

    private fun refresh() {
        consume(engine?.listExtensions(), "List") { items ->
            shown.clear()
            shown.addAll(items.orEmpty())
            adapter.clear()
            if (shown.isEmpty()) {
                adapter.add(getString(R.string.no_extensions))
            } else {
                shown.forEach { adapter.add("${it.id}\n${it.location}") }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun confirmUninstall(ext: WebExtension) {
        AlertDialog.Builder(this)
            .setMessage("Uninstall ${ext.id}?")
            .setPositiveButton(R.string.uninstall) { _, _ ->
                consume(engine?.uninstallExtension(ext), "Uninstall") {
                    status.text = "Uninstalled: ${ext.id}"
                    refresh()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroy() {
        // Shared engine outlives this screen; nothing to tear down.
        engine = null
        super.onDestroy()
    }
}
