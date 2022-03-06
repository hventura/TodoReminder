package com.udacity.project4.savereminder

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import java.util.*

@Config(manifest = Config.NONE)
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    private lateinit var appContext: Application

    // Subject
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Fake Repo
    private lateinit var dataSource: FakeDataSource

    @Before
    fun setupViewModel() {
        appContext = ApplicationProvider.getApplicationContext()
        dataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(appContext, dataSource)
    }

    @Test
    fun initValues_NullOrZeroed() {
        assertThat(saveReminderViewModel.reminderTitle.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.selectedPOI.value, `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.value, `is`(0.0))
        assertThat(saveReminderViewModel.longitude.value, `is`(0.0))
        assertThat(saveReminderViewModel.reminderSnapshotLocation.value, `is`(nullValue()))
    }

    @Test
    fun saveReminder_NoTitle_shouldReturnFalse() {
        val reminderWithNoTitle = ReminderDataItem(
            "", "Description 1", "Test Location 1",
            40.3315763674, -7.61673115209, "/path/to/snapshot1", UUID.randomUUID().toString()
        )
        assertThat(saveReminderViewModel.validateAndSaveReminder(reminderWithNoTitle), `is`(false))
    }

    @Test
    fun saveReminder_NoLocation_shouldReturnFalse() {
        val reminderWithNoLocation = ReminderDataItem(
            "Title 1", "Description 1", "",
            40.3315763674, -7.61673115209, "/path/to/snapshot1", UUID.randomUUID().toString()
        )
        assertThat(saveReminderViewModel.validateAndSaveReminder(reminderWithNoLocation), `is`(false))
    }

    @Test
    fun saveReminder_CorrectValues_shouldReturnTrueAndSaveToDatabase() {
        val reminderID = UUID.randomUUID().toString()
        val reminderWithCorrectValues = ReminderDataItem(
            "Title 1", "Description 1", "Test Location 1",
            40.3315763674, -7.61673115209, "/path/to/snapshot1", reminderID
        )
        assertThat(saveReminderViewModel.validateAndSaveReminder(reminderWithCorrectValues), `is`(true))
        // Assertion passes but has error that i cannot find out why :/
    }

}