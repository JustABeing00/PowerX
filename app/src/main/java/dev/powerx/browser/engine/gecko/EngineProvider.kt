package dev.powerx.browser.engine.gecko

import android.content.Context
import dev.powerx.browser.engine.BrowserEngine

/**
 * Process-wide engine singleton. GeckoRuntime is heavyweight and must exist
 * exactly once per process; every Activity shares this instance. UI state
 * (views, listeners) stays per-Activity; the runtime and session are shared.
 * A future Chromium engine module plugs in behind the same provider.
 */
object EngineProvider {

    private var engine: GeckoBrowserEngine? = null

    @Synchronized
    fun init(context: Context): GeckoBrowserEngine {
        var current = engine
        if (current == null) {
            current = GeckoBrowserEngine(context.applicationContext)
            engine = current
        }
        return current
    }

    fun require(): GeckoBrowserEngine =
        engine ?: throw IllegalStateException("EngineProvider.init not called")

    /** Test/support hook: is the runtime already up in this process? */
    fun isInitialized(): Boolean = engine != null
}
