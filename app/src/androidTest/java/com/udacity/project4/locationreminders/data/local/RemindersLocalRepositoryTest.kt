package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result.Success
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @Test
    fun saveReminder_retrieveById() = runBlocking {
        val reminder1 = ReminderDTO(
            "Title 1", "Description 1", "Test Location 1",
            40.3315763674, -7.61673115209, "/path/to/snapshot1", UUID.randomUUID().toString()
        )
        repository.saveReminder(reminder1)

        val loaded = repository.getReminder(reminder1.id)

        assertThat(loaded is Success, `is`(true))
        loaded as Success<ReminderDTO>
        assertThat(loaded.data.id, `is`(reminder1.id))
        assertThat(loaded.data.title, `is`(reminder1.title))
        assertThat(loaded.data.description, `is`(reminder1.description))
        assertThat(loaded.data.location, `is`(reminder1.location))
        assertThat(loaded.data.latitude, `is`(reminder1.latitude))
        assertThat(loaded.data.longitude, `is`(reminder1.longitude))
        assertThat(loaded.data.snapshot, `is`(reminder1.snapshot))
    }

    @Test
    fun deleteAllReminders() = runBlocking {
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
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)
        repository.saveReminder(reminder3)
        repository.deleteAllReminders()

        val loaded = repository.getReminders()

        assertThat(loaded is Success, `is`(true))
        loaded as Success<List<ReminderDTO>>
        assertThat(loaded.data, `is`(emptyList()))

    }

    @After
    fun cleanUp() {
        database.close()
    }


}