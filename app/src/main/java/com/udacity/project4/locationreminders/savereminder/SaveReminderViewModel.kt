package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) : BaseViewModel(app) {

    var hourOfDay = MutableLiveData(DateTime().hourOfDay)
    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()
    val selectedPOI = MutableLiveData<PointOfInterest?>()
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()
    val reminderSnapshotLocation = MutableLiveData<String?>()

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    private fun resetData() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = 0.0
        longitude.value = 0.0
        reminderSnapshotLocation.value = null
    }

    init {
        resetData()
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem): Boolean {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
            return true
        }
        return false
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.snapshot,
                    reminderData.id
                )
            )
            showLoading.value = false
            showSnackBar.value = app.getString(R.string.reminder_saved)
            resetData()
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     * Had to remove the private fun to be able to control when to validate and only save to DB after adding Geofence
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

    override fun onCleared() {
        super.onCleared()
        resetData()
    }
}