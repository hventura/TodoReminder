package pt.hventura.todoreminder.locationreminders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.navArgs
import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.databinding.ActivityReminderDescriptionBinding
import pt.hventura.todoreminder.locationreminders.geofence.GeofenceBroadcastReceiver
import pt.hventura.todoreminder.locationreminders.reminderslist.ReminderDataItem
import pt.hventura.todoreminder.utils.Constants
import timber.log.Timber

class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        // receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private val args: ReminderDescriptionActivityArgs by navArgs()
    private lateinit var binding: ActivityReminderDescriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = args.reminder.title
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_description)

        // TODO: Check this
        // val reminderDataItem = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem
        // binding.reminderDataItem = reminderDataItem
        Timber.i(args.toString())
        Timber.i(intent.getSerializableExtra(EXTRA_ReminderDataItem).toString())
        binding.reminderDataItem = args.reminder

        Timber.i(args.reminder.snapshot)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}