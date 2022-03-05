package pt.hventura.todoreminder.locationreminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import org.koin.androidx.viewmodel.ext.android.viewModel
import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.authentication.AuthenticationActivity
import pt.hventura.todoreminder.base.BaseFragment
import pt.hventura.todoreminder.base.NavigationCommand
import pt.hventura.todoreminder.databinding.FragmentReminderListBinding
import pt.hventura.todoreminder.locationreminders.ReminderDescriptionActivity
import pt.hventura.todoreminder.utils.setDisplayHomeAsUpEnabled
import pt.hventura.todoreminder.utils.setup
import timber.log.Timber

class ReminderListFragment : BaseFragment() {
    // Using Koin to retrieve the ViewModel instance
    override val viewModel: RemindersListViewModel by viewModel()

    private lateinit var binding: FragmentReminderListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_reminder_list, container, false)
        binding.viewModel = viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)

        binding.refreshLayout.setOnRefreshListener {
            viewModel.loadReminders()
            binding.refreshLayout.isRefreshing = false
        }

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
        // load the reminders list on the ui
        viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        // use the navigationCommand live data to navigate between the fragments
        viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.actionReminderListFragmentToSaveReminderFragment()
            )
        )
    }

    private fun setupRecyclerView() {
        // The RemindersListAdapter is waiting for a callback that receives a reminderDataItem,
        // configured by the BaseRecyclerViewAdapter -> holder.itemView.setOnClickListener
        val adapter = RemindersListAdapter {
            val intent = Intent(requireActivity(), ReminderDescriptionActivity::class.java)
            intent.putExtra("EXTRA_ReminderDataItem", it)
            startActivity(intent)
        }

        // setup the recycler view using the extension function
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
            R.id.resetReminders -> {
                Timber.i("Clicked reset reminders")
                showDialog()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.sure_to_delete)
            .setPositiveButton(R.string.i_am_sure) { dialogInterface, _ ->
                viewModel.resetRemindersList()
                viewModel.loadReminders()
                dialogInterface.dismiss()
            }
            .setNegativeButton(R.string.not_sure) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        builder.create().show()
    }

}