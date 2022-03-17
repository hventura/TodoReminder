package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.data.UserData
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.PreferencesManager

class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        // receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem, fromIntent: Boolean): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            intent.putExtra("fromIntent", fromIntent)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    private var fromIntent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val reminderDataItem = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem
        fromIntent = intent.getBooleanExtra("fromIntent", false)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = reminderDataItem.title
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_description)
        binding.reminderDataItem = reminderDataItem
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (fromIntent) {
                    if (PreferencesManager.retrieve<UserData>("userData") != null) {
                        startActivity(Intent(this, RemindersActivity::class.java))
                    } else {
                        startActivity(Intent(this, AuthenticationActivity::class.java))
                    }
                } else {
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}