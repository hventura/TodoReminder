package com.udacity.project4.reminderslist

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import java.util.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Subject
    private lateinit var remindersListViewModel: RemindersListViewModel

    // Fake Repo
    private lateinit var dataSource: FakeDataSource

    @Before
    fun setupViewModel() {
        dataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @Test
    fun checkEmptyList_showsNoData() {
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_loading() {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun checkPopulatedList_showNoDataFalse() {
        val reminder1 = ReminderDTO(
            "Title 1", "Description 1", "Test Location 1",
            40.3315763674, -7.61673115209, "/path/to/snapshot1", UUID.randomUUID().toString()
        )
        val reminder2 = ReminderDTO(
            "Title 2", "Description 2", "Test Location 2",
            37.4183538601, -122.0880212345, "/path/to/snapshot2", UUID.randomUUID().toString()
        )
        val reminder3 = ReminderDTO(
            "Title 3", "Description 3", "Test Location 3",
            21.33829773771, -158.124750259, "/path/to/snapshot3", UUID.randomUUID().toString()
        )
        dataSource.saveReminders(reminder1, reminder2, reminder3)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun checkPopulatedList_shouldReturnError() = mainCoroutineRule.runBlockingTest {
        dataSource.setReturnError(true)
        val reminder1 = ReminderDTO(
            "Title 1", "Description 1", "Test Location 1",
            40.3315763674, -7.61673115209, "/path/to/snapshot1", UUID.randomUUID().toString()
        )
        dataSource.saveReminder(reminder1)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Test Exception"))
    }

    @After
    fun resetData() {
        mainCoroutineRule.runBlockingTest {
            dataSource.deleteAllReminders()
        }
    }

}