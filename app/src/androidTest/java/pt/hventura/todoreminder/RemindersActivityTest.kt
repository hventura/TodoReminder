package pt.hventura.todoreminder

import android.app.Application
import android.os.Handler
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import pt.hventura.todoreminder.locationreminders.RemindersActivity
import pt.hventura.todoreminder.locationreminders.data.ReminderDataSource
import pt.hventura.todoreminder.locationreminders.data.local.LocalDB
import pt.hventura.todoreminder.locationreminders.data.local.RemindersLocalRepository
import pt.hventura.todoreminder.locationreminders.reminderslist.RemindersListViewModel
import pt.hventura.todoreminder.locationreminders.savereminder.SaveReminderViewModel
import pt.hventura.todoreminder.util.DataBindingIdlingResource
import pt.hventura.todoreminder.util.EspressoIdlingResource
import pt.hventura.todoreminder.util.monitorActivity

@RunWith(AndroidJUnit4::class)
@LargeTest
@ExperimentalCoroutinesApi
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
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


    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    // TODO: Tests here
    @Test
    fun createOneReminder_checkIsDisplayed() = runBlocking {
        //start RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //create new reminder
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("TITLE_A"))
        onView(withId(R.id.reminderDescription)).perform(typeText("DESCRIPTION_A"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.selectLocation)).perform(click())
        delay(5000) // wait for animation to user location
        onView(withId(R.id.map)).perform(click())
        delay(5000) // wait for animation to user selection (and snapshot)
        onView(withId(R.id.confirm_button)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText("TITLE_A")).check(matches(isDisplayed()))
        activityScenario.close()
    }


    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        runBlocking {
            repository.deleteAllReminders()
        }
    }

}