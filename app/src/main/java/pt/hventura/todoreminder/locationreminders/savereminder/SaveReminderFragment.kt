package pt.hventura.todoreminder.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.base.BaseFragment
import pt.hventura.todoreminder.base.NavigationCommand
import pt.hventura.todoreminder.databinding.FragmentSaveReminderBinding
import pt.hventura.todoreminder.locationreminders.geofence.GeofenceBroadcastReceiver
import pt.hventura.todoreminder.locationreminders.reminderslist.ReminderDataItem
import pt.hventura.todoreminder.utils.Constants
import pt.hventura.todoreminder.utils.Constants.GEOFENCE_RADIUS_METERS
import pt.hventura.todoreminder.utils.Constants.runningQOrLater
import pt.hventura.todoreminder.utils.setDisplayHomeAsUpEnabled
import timber.log.Timber

class SaveReminderFragment : BaseFragment() {

    //Get the view model this time as a single to be shared with the another fragment
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private var foregroundLocationApproved = false
    private var backgroundPermissionApproved = false
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = Constants.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @TargetApi(29)
    private val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        foregroundLocationApproved = (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) &&
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false))
        backgroundPermissionApproved = if (runningQOrLater) {
            permissions.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false)
        } else {
            true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_save_reminder, container, false)
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //Navigate to another fragment to select location
            viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        requestPermission()

        binding.saveReminder.setOnClickListener {
            checkPermission()
        }
    }

    private fun checkPermission() {
        if (foregroundLocationApproved && backgroundPermissionApproved) {
            Timber.i("We have all we need, go ahead!")
            saveAndAddGeofence()
        } else {
            Snackbar.make(
                binding.clSaveReminder,
                R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                requestPermission()
            }.show()
        }
    }

    @TargetApi(29)
    private fun requestPermission() {
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        if (runningQOrLater) {
            permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }

        locationPermissionRequest.launch(permissionsArray)
    }

    @SuppressLint("MissingPermission")
    private fun saveAndAddGeofence() {
        val title = viewModel.reminderTitle.value
        val description = viewModel.reminderDescription.value
        val location = viewModel.reminderSelectedLocationStr.value
        val latitude = viewModel.latitude.value
        val longitude = viewModel.longitude.value
        val snapshot = viewModel.reminderSnapshotLocation.value

        val reminder = ReminderDataItem(
            title, description, location, latitude!!, longitude!!, snapshot
        )

        if (viewModel.validateAndSaveReminder(reminder)) {
            // DONE: Add geofence if validateAndSaveReminder() is OK
            val geofence = Geofence.Builder()
                .setRequestId(reminder.id)
                .setCircularRegion(
                    reminder.latitude,
                    reminder.longitude,
                    GEOFENCE_RADIUS_METERS
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent).run {
                addOnSuccessListener {
                    viewModel.showSnackBar.value = getString(R.string.geofence_added)
                    viewModel.navigationCommand.value = NavigationCommand.Back
                }
                addOnFailureListener {
                    viewModel.showErrorMessage.value = getString(R.string.geofence_not_added)
                }
            }

        }

    }

    /**
     * We should remove the geofence once we do not need it ...
     * My idea was to remove it once we clicked or discard the notification and entered the ReminderDescriptionActivity
     * But not sure how to do it :(
     */
    private fun removeGeofence() {
        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnSuccessListener {
                viewModel.showSnackBar.value = getString(R.string.geofence_removed)
            }
            addOnFailureListener {
                viewModel.showSnackBar.value = getString(R.string.geofence_not_removed)
            }
        }
    }
}