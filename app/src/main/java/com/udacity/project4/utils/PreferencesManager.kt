package com.udacity.project4.utils

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder

/**
 * Singleton class for managing preferences for POJO or model class's object.
 * Got it from here
 * @url https://gist.github.com/malwinder-s/bf2292bcdda73d7076fc080c03724e8a
 */
object PreferencesManager {

    //Shared Preference field used to save and retrieve JSON string
    lateinit var preferences: SharedPreferences

    //Name of Shared Preference file
    private const val PREFERENCES_REMINDER_APP = "PREFERENCES_REMINDER_APP"

    fun with(application: Application) {
        preferences = application.getSharedPreferences(
            PREFERENCES_REMINDER_APP, Context.MODE_PRIVATE
        )
    }

    /**
     * Saves object into the Preferences.
     *
     * @param `object` Object of model class (of type [T]) to save
     * @param key Key with which Shared preferences to
     **/
    fun <T> put(`object`: T, key: String) {
        val jsonString = GsonBuilder().create().toJson(`object`)
        preferences.edit().putString(key, jsonString).apply()
    }

    /**
     * Used to retrieve object from the Preferences.
     *
     * @param key Shared Preference key with which object was saved.
     **/
    inline fun <reified T> retrieve(key: String): T? {
        //We read JSON String which was saved.
        val value = preferences.getString(key, null)
        return try {
            GsonBuilder().create().fromJson(value, T::class.java)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}