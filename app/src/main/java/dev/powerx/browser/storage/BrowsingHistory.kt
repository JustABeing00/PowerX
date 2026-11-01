package dev.powerx.browser.storage

import android.content.Context
import org.json.JSONArray

/** Minimal history store (SharedPreferences-backed). Room migration in Phase 4. */
class BrowsingHistory(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    data class Entry(val url: String, val title: String?, val at: Long)

    fun record(url: String, title: String?) {
        if (url.isBlank()) return
        val entries = all().toMutableList()
        entries.add(0, Entry(url, title, System.currentTimeMillis()))
        while (entries.size > MAX) entries.removeLast()
        val json = JSONArray()
        for (e in entries) {
            json.put("${e.at}\u0001${e.url}\u0001${e.title ?: ""}")
        }
        prefs.edit().putString(KEY, json.toString()).apply()
    }

    fun all(): List<Entry> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            List(json.length()) { i ->
                val parts = json.getString(i).split("\u0001")
                Entry(parts[1], parts.getOrNull(2)?.ifBlank { null }, parts[0].toLong())
            }
        }.getOrDefault(emptyList())
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        private const val PREFS = "powerx_history"
        private const val KEY = "entries"
        private const val MAX = 200
    }
}
