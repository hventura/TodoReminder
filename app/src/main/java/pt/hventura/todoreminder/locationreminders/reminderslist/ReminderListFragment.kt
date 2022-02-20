package pt.hventura.todoreminder.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.authentication.AuthenticationActivity
import pt.hventura.todoreminder.base.BaseFragment
import pt.hventura.todoreminder.base.NavigationCommand
import pt.hventura.todoreminder.databinding.FragmentReminderListBinding
import pt.hventura.todoreminder.utils.setDisplayHomeAsUpEnabled
import pt.hventura.todoreminder.utils.setTitle
import pt.hventura.todoreminder.utils.setup

class ReminderListFragment : BaseFragment() {
    // Using Koin to retrieve the ViewModel instance
    override val mViewModel: RemindersListViewModel by viewModel()

    private lateinit var binding: FragmentReminderListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_reminder_list, container, false)
        binding.viewModel = mViewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { mViewModel.loadReminders() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        mViewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        mViewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.actionReminderListFragmentToSaveReminderFragment()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

//        setup the recycler view using the extension function
        binding.remindersRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance()
                    .signOut(requireContext())
                    .addOnCompleteListener {
                        startActivity(Intent(requireActivity(), AuthenticationActivity::class.java))
                        requireActivity().finish()
                    }
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

}