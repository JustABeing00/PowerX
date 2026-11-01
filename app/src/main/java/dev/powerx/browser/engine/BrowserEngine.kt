package dev.powerx.browser.engine

import android.view.ViewGroup

/**
 * Honest capability description for the active engine.
 * The UI renders these labels verbatim — capabilities are never faked.
 */
data class ExtensionCapabilities(
    val firefoxWebExtensions: Boolean,
    val chromeWebExtensions: Boolean,
    val installFromUrl: Boolean,
    val unpackedSideload: Boolean,
    val userFacingLabel: String
)

/**
 * Engine boundary. All app code reaches web content through this interface;
 * engine SDK types must not leak outside the engine subpackage implementations.
 * A future Chromium module implements this same contract.
 */
interface BrowserEngine {
    val name: String
    val extensionCapabilities: ExtensionCapabilities

    fun attach(host: ViewGroup)
    fun loadUrl(url: String)
    fun goBack(): Boolean
    fun goForward(): Boolean
    fun reload()
    fun setListener(listener: EngineListener?)
    fun destroy()

    interface EngineListener {
        fun onProgress(progress: Int)
        fun onLocation(url: String?)
        fun onTitle(title: String?)
        fun onNavigationState(canGoBack: Boolean, canGoForward: Boolean)
    }
}
