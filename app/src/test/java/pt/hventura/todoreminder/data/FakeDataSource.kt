package pt.hventura.todoreminder.data

import pt.hventura.todoreminder.locationreminders.data.ReminderDataSource
import pt.hventura.todoreminder.locationreminders.data.dto.ReminderDTO
import pt.hventura.todoreminder.locationreminders.data.dto.Result
import pt.hventura.todoreminder.locationreminders.data.dto.Result.Error
import pt.hventura.todoreminder.locationreminders.data.dto.Result.Success

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var remindersList: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

// TODO: Create a fake data source to act as a double to the real data source

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Error("Test Exception")
        }
        if (remindersList.isNullOrEmpty()) {
            return Error("List is empty")
        }
        return Success(remindersList!!.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList?.add(reminder)
    }

    fun saveReminders(vararg reminders: ReminderDTO) {
        for (reminder in reminders) {
            remindersList?.add(reminder)
        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Error("Test exception")
        }
        var reminderFound: ReminderDTO?
        remindersList?.let {
            reminderFound = it.find { reminderDTO ->
                reminderDTO.id == id
            }
            return if (reminderFound != null) Success(reminderFound!!) else Error("Reminder not found")
        }
        return Error("Could not find reminder with provided ID")
    }

    override suspend fun deleteAllReminders() {
        remindersList?.clear()
    }

}