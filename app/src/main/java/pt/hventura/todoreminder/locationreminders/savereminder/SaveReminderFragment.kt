package pt.hventura.todoreminder.locationreminders.savereminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.koin.android.ext.android.inject
import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.base.BaseFragment
import pt.hventura.todoreminder.base.NavigationCommand
import pt.hventura.todoreminder.databinding.FragmentSaveReminderBinding
import pt.hventura.todoreminder.locationreminders.reminderslist.ReminderDataItem
import pt.hventura.todoreminder.utils.setDisplayHomeAsUpEnabled
import timber.log.Timber

class SaveReminderFragment : BaseFragment() {

    //Get the view model this time as a single to be shared with the another fragment
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

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

        binding.saveReminder.setOnClickListener {
            saveAndAddGeofence()
        }
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
        }
    }

}