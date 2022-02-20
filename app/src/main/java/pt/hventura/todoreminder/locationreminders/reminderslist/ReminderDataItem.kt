package pt.hventura.todoreminder.locationreminders.reminderslist

import java.io.Serializable
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
data class ReminderDataItem(
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
    var snapshot: String?,
    val id: String = UUID.randomUUID().toString()
) : Serializable {
    fun getStringLatitude(): String {
        return String.format("%.5f", latitude)
    }

    fun getStringLongitude(): String {
        return String.format("%.5f", longitude)
    }
}