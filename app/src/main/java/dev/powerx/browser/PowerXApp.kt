package dev.powerx.browser

import android.app.Application
import dev.powerx.browser.engine.gecko.EngineProvider

/** Initializes the process-wide browser engine exactly once. */
class PowerXApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // GeckoView spawns private child processes (:tab_*, :gpu_*) that also
        // run Application.onCreate. A runtime must only ever be created in
        // the main process — creating one in a child kills the browser.
        if (isMainProcess()) {
            EngineProvider.init(this)
        }
    }

    private fun isMainProcess(): Boolean {
        val name = try {
            Application.getProcessName()
        } catch (_: Throwable) {
            null
        }
        return name == null || name == packageName
    }
}
