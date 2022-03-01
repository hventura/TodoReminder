package pt.hventura.todoreminder.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.base.BaseFragment
import pt.hventura.todoreminder.base.NavigationCommand
import pt.hventura.todoreminder.databinding.FragmentSaveReminderBinding
import pt.hventura.todoreminder.locationreminders.reminderslist.ReminderDataItem
import pt.hventura.todoreminder.utils.Constants.runningQOrLater
import pt.hventura.todoreminder.utils.setDisplayHomeAsUpEnabled
import timber.log.Timber

class SaveReminderFragment : BaseFragment() {

    //Get the view model this time as a single to be shared with the another fragment
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private var foregroundLocationApproved = false
    private var backgroundPermissionApproved = false

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
            //Navigate to another fragment to get the user location
            viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

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

    private fun saveAndAddGeofence() {
        val title = viewModel.reminderTitle.value
        val description = viewModel.reminderDescription.value
        val location = viewModel.reminderSelectedLocationStr.value
        val latitude = viewModel.latitude.value
        val longitude = viewModel.longitude.value
        val snapshot = viewModel.reminderSnapshotLocation.value
        var locationString = "No info selected"

        val reminder = ReminderDataItem(
            title, description, location, latitude, longitude, snapshot
        )

        if (viewModel.validateAndSaveReminder(reminder)) {
            Timber.i(
                "->\nWill use the user entered reminder details to:\n" +
                        "1) add a geofencing request\n2) save the reminder to the local db"
            )
            location?.let {
                locationString = reminder.getStringLocation()
            }
            Timber.i("->\nWill save: $title - $description - Location: $locationString")
            Timber.i("->\nSnapShot Location: $snapshot")

            // TODO: Add geofence if validateAndSaveReminder() is OK
        }

    }

}