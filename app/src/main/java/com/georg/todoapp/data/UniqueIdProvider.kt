package com.georg.todoapp.data

import android.content.SharedPreferences

class UniqueIdProvider(
    private val sharedPreferences: SharedPreferences,
) : IUniqueIdProvider {
    companion object {
        private const val LAST_ID_KEY = "last_id"
    }

    /**
     * Get a unique ID.
     * The ID is guaranteed to be unique even after the app is closed and restarted.
     */
    override fun getUniqueId(): Int {
        val lastId = sharedPreferences.getInt(LAST_ID_KEY, 0)
        val newId = lastId + 1

        // Save the new ID
        sharedPreferences.edit().putInt(LAST_ID_KEY, newId).apply()

        return newId
    }
}
