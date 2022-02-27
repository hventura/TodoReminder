package pt.hventura.todoreminder.locationreminders.reminderslist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.hventura.todoreminder.base.BaseViewModel
import pt.hventura.todoreminder.base.NavigationCommand
import pt.hventura.todoreminder.locationreminders.data.ReminderDataSource
import pt.hventura.todoreminder.locationreminders.data.dto.ReminderDTO
import pt.hventura.todoreminder.locationreminders.data.dto.Result.Error
import pt.hventura.todoreminder.locationreminders.data.dto.Result.Success

class RemindersListViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            showLoading.postValue(false)
            when (result) {
                is Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    dataList.addAll((result.data as List<ReminderDTO>).map { reminder ->
                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.snapshot,
                            reminder.id
                        )
                    })
                    remindersList.value = dataList
                }
                is Error ->
                    showSnackBar.value = result.message
            }

            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }

    /**
     * Delete All Reminders from Database
     */
    fun resetRemindersList() {
        viewModelScope.launch {
            dataSource.deleteAllReminders()
        }
    }

}