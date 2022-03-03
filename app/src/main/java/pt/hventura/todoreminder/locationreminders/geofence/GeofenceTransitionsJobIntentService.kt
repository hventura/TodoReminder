package pt.hventura.todoreminder.locationreminders.geofence

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import pt.hventura.todoreminder.locationreminders.data.dto.ReminderDTO
import pt.hventura.todoreminder.locationreminders.data.dto.Result
import pt.hventura.todoreminder.locationreminders.data.local.RemindersLocalRepository
import pt.hventura.todoreminder.locationreminders.reminderslist.ReminderDataItem
import pt.hventura.todoreminder.utils.sendNotification
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 8487

        //call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, GeofenceTransitionsJobIntentService::class.java, JOB_ID, intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        // handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        // then call @sendReminderNotification
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            Timber.e("Something went wrong: ${geofencingEvent.errorCode}")
            return
        }

        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Timber.i("Entered geofence!")
            sendReminderNotification(geofencingEvent.triggeringGeofences)
        }
        // For now we don't want to do anything else with the geofence.
        // We could do something different with the geofenceTransitions here..
        // https://developer.android.com/training/location/geofencing
        // https://developers.google.com/android/reference/com/google/android/gms/location/GeofencingEvent

    }

    //get the request id of the current geofence
    private fun sendReminderNotification(geofenceList: List<Geofence>) {
        // Its a list so we can have multiple geofence. How to handle if more than one?
        // Option 1 - For each geofence, we send a notification.
        // Option 2 - We ignore and show notification only to the 1st of the list.
        // Because Option 2  is not the best practice (we shouldn't ignore user expectation of the usability of app)
        // I opt  for Option 1.

        // We only need one instance of repository
        val remindersLocalRepository: RemindersLocalRepository by inject()

        //And as the interaction to the repository has to be through a coroutine scope
        //We do this all inside coroutine scope:
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //Iterate every geofence registered
            for (geofence in geofenceList) {
                val requestId = geofence.requestId
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.snapshot,
                            reminderDTO.id
                        )
                    )
                }
            }
        }

    }

}