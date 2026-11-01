package dev.powerx.browser.engine.gecko

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import dev.powerx.browser.engine.BrowserEngine
import dev.powerx.browser.engine.ExtensionCapabilities
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtensionController

/**
 * GeckoView proof-of-concept engine (MPL-2.0 dependency, used unmodified).
 * Runs Firefox WebExtensions — NOT Chrome extensions; the capability label
 * states this explicitly and the UI must surface it.
 *
 * Navigation-state queries use delegate-tracked flags because this
 * GeckoView release exposes back/forward availability via
 * onCanGoBack/onCanGoForward callbacks.
 */
class GeckoBrowserEngine(context: Context) : BrowserEngine {

    override val name: String = "GeckoView"

    override val extensionCapabilities = ExtensionCapabilities(
        firefoxWebExtensions = true,
        chromeWebExtensions = false,
        installFromUrl = true,
        unpackedSideload = false,
        userFacingLabel = "Firefox extensions (AMO). Chrome Web Store: unsupported on this engine."
    )

    private val appContext = context.applicationContext
    private val runtime: GeckoRuntime = GeckoRuntime.create(appContext)
    private val session = GeckoSession()
    private var view: GeckoView? = null
    private var listener: BrowserEngine.EngineListener? = null
    private var opened = false
    private var canGoBackState = false
    private var canGoForwardState = false

    private val progressDelegate = object : GeckoSession.ProgressDelegate {
        override fun onProgressChange(session: GeckoSession, progress: Int) {
            listener?.onProgress(progress)
        }
    }

    private val navigationDelegate = object : GeckoSession.NavigationDelegate {
        override fun onLocationChange(
            session: GeckoSession,
            url: String?,
            perms: List<GeckoSession.PermissionDelegate.ContentPermission>,
            hasUserGesture: Boolean
        ) {
            listener?.onLocation(url)
            listener?.onNavigationState(canGoBackState, canGoForwardState)
        }

        override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
            canGoBackState = canGoBack
            listener?.onNavigationState(canGoBackState, canGoForwardState)
        }

        override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
            canGoForwardState = canGoForward
            listener?.onNavigationState(canGoBackState, canGoForwardState)
        }
    }

    private val contentDelegate = object : GeckoSession.ContentDelegate {
        override fun onTitleChange(session: GeckoSession, title: String?) {
            listener?.onTitle(title)
        }
    }

    override fun attach(host: ViewGroup) {
        if (!opened) {
            session.progressDelegate = progressDelegate
            session.navigationDelegate = navigationDelegate
            session.contentDelegate = contentDelegate
            session.open(runtime)
            opened = true
        }
        val gv = GeckoView(appContext)
        gv.setSession(session)
        host.addView(
            gv, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        view = gv
    }

    override fun loadUrl(url: String) {
        session.loadUri(url)
    }

    override fun goBack(): Boolean {
        if (canGoBackState) {
            session.goBack()
            return true
        }
        return false
    }

    override fun goForward(): Boolean {
        if (canGoForwardState) {
            session.goForward()
            return true
        }
        return false
    }

    override fun reload() {
        session.reload()
    }

    override fun setListener(listener: BrowserEngine.EngineListener?) {
        this.listener = listener
    }

    override fun destroy() {
        runCatching { session.close() }
        view = null
    }

    // ---- Firefox-extension operations (engine-backed, real) ----

    fun installExtension(url: String): GeckoResult<WebExtension> =
        runtime.webExtensionController.install(url)

    fun listExtensions(): GeckoResult<List<WebExtension>> =
        runtime.webExtensionController.list()

    fun uninstallExtension(extension: WebExtension): GeckoResult<Void> =
        runtime.webExtensionController.uninstall(extension)

    fun setPromptDelegate(delegate: WebExtensionController.PromptDelegate?) {
        runtime.webExtensionController.setPromptDelegate(delegate)
    }
}
