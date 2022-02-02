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
import pt.hventura.todoreminder.utils.setDisplayHomeAsUpEnabled
import timber.log.Timber

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val mViewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_save_reminder,
            container,
            false
        )
        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = mViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //Navigate to another fragment to get the user location
            mViewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = mViewModel.reminderTitle.value
            val description = mViewModel.reminderDescription.value
            val location = mViewModel.reminderSelectedLocationStr.value
            val latitude = mViewModel.latitude.value
            val longitude = mViewModel.longitude.value
            var locationString = "No info selected"

            Timber.i(
                "->\nWill use the user entered reminder details to:\n" +
                        "1) add a geofencing request\n2) save the reminder to the local db"
            )
            location?.let {
                locationString = "$location ($latitude, $longitude)"
            }
            Timber.i("->\nWill save: $title - $description - Location: $locationString")
        }
    }
}