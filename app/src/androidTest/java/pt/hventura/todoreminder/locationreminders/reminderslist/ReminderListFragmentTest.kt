package pt.hventura.todoreminder.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.locationreminders.data.ReminderDataSource
import pt.hventura.todoreminder.locationreminders.data.dto.ReminderDTO
import pt.hventura.todoreminder.locationreminders.data.dto.asDataItem
import pt.hventura.todoreminder.locationreminders.data.local.LocalDB
import pt.hventura.todoreminder.locationreminders.data.local.RemindersLocalRepository
import pt.hventura.todoreminder.locationreminders.savereminder.SaveReminderViewModel
import pt.hventura.todoreminder.util.DataBindingIdlingResource
import java.util.*

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun DataBindingIdlingResource.monitorReminderListFragment(fragmentScenario: FragmentScenario<ReminderListFragment>) {
        fragmentScenario.onFragment { fragment -> activity = fragment.requireActivity() }
    }

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    //    TODO: test the navigation of the fragments.
    @Test
    fun remindersList_clickAdd_GotoSaveReminderFragment() {
        //1st used this: https://developer.android.com/guide/navigation/navigation-testing
        // After some digging: https://www.youtube.com/watch?v=uUmFfZDoOTY

        //val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val navController = mock(NavController::class.java)
        val reminderListScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.TodoReminderTheme)
        reminderListScenario.onFragment { fragment ->
            navController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        onView(withId(R.id.addReminderFAB)).perform(click())
        //assertThat(navController.currentDestination!!.id, `is`(R.id.saveReminderFragment))
        verify(navController).navigate(ReminderListFragmentDirections.actionReminderListFragmentToSaveReminderFragment())
    }

    //    TODO: test the displayed data on the UI.
    @Test
    fun remindersList_clickReminder_showDetails() = runBlocking {
        val reminder1 = ReminderDTO(
            "Title 1", "Description 1", "Test Location 1",
            40.3315763674, -7.61673115209, "/path/to/snapshot1", UUID.randomUUID().toString()
        )

        repository.saveReminder(reminder1)

        val navController = mock(NavController::class.java)
        val reminderListScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.TodoReminderTheme)
        reminderListScenario.onFragment { fragment ->
            navController.setGraph(R.navigation.nav_graph)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }
        dataBindingIdlingResource.monitorReminderListFragment(reminderListScenario)

        onView(withId(R.id.remindersRecyclerView)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Title 1")), click()
            )
        )

        verify(navController).navigate(ReminderListFragmentDirections.actionReminderListFragmentToReminderDescriptionActivity(reminder1.asDataItem()))

    }

    //    TODO: add testing for the error messages.
    @Test
    fun reminderList_showNoData() {
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.TodoReminderTheme)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @After
    fun cleanUp() = runTest {
        // As i am always using the same reminder1 values it is best to delete all on starting and ending tests.
        repository.deleteAllReminders()
    }
}