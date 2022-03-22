package com.udacity.project4

import android.app.Application
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
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
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
@LargeTest
@ExperimentalCoroutinesApi
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private val device = UiDevice.getInstance(getInstrumentation())

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

    // Tests here
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
        allowLocationPermissionsIfNeeded()
        delay(4000) // wait for animation to user location
        onView(withId(R.id.map)).perform(click())
        delay(4000) // wait for animation to user selection (and snapshot)
        onView(withId(R.id.confirm_button)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        allowBackgroundLocationPermissionsIfNeeded()
        delay(2000) // I have an Handler().postDelay() in between this
        onView(withText("TITLE_A")).check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun createOneReminder_checkSnackBar() = runBlocking {
        //start RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //create new reminder
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("TITLE_A"))
        onView(withId(R.id.reminderDescription)).perform(typeText("DESCRIPTION_A"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.selectLocation)).perform(click())
        allowLocationPermissionsIfNeeded()
        delay(5000) // wait for animation to user location
        onView(withId(R.id.map)).perform(click())
        delay(5000) // wait for animation to user selection (and snapshot)
        onView(withId(R.id.confirm_button)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        allowBackgroundLocationPermissionsIfNeeded()
        delay(500) // wait for permissions dialog if needed
        //check the snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.reminder_saved)))

        activityScenario.close()
    }

    // Not passing due to?: https://github.com/android/android-test/issues/803
    @Test
    fun createOneReminder_checkToast() = runBlocking {
        //start RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //create new reminder
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("TITLE_A"))
        onView(withId(R.id.reminderDescription)).perform(typeText("DESCRIPTION_A"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.selectLocation)).perform(click())
        allowLocationPermissionsIfNeeded()
        delay(5000) // wait for animation to user location
        onView(withId(R.id.map)).perform(click())
        delay(5000) // wait for animation to user selection (and snapshot)
        onView(withId(R.id.confirm_button)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        allowBackgroundLocationPermissionsIfNeeded()
        //check Toast
        delay(1000)
        onView(withText(R.string.geofence_added)).inRoot(withDecorView(not(getActivity(appContext)?.window?.decorView))).check(matches(isDisplayed()))
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

    @VisibleForTesting
    private fun allowLocationPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val allowPermissions = device.findObject(UiSelector().text("Allow only while using the app"))
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click()
                } catch (e: UiObjectNotFoundException) {
                    Timber.e(e, "There is no permissions dialog to interact with")
                }
            }
        }
    }

    @VisibleForTesting
    private fun allowBackgroundLocationPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val allowPermissions = device.findObject(UiSelector().text("Allow all the time"))
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click()
                } catch (e: UiObjectNotFoundException) {
                    Timber.e(e, "There is no permissions dialog to interact with")
                }
            }
        }
    }

}