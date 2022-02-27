package pt.hventura.todoreminder.locationreminders

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.authentication.AuthenticationActivity
import pt.hventura.todoreminder.databinding.ActivityRemindersBinding
import pt.hventura.todoreminder.utils.PreferencesManager
import timber.log.Timber

class RemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminders)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController(R.id.nav_host_fragment).popBackStack()
                return true
            }
            R.id.logout -> {
                PreferencesManager.put(null, "userData")
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, AuthenticationActivity::class.java))
                overridePendingTransition(0, 0)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}