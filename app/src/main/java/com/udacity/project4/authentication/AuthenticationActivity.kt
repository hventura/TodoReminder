package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.data.UserData
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.utils.Constants.SIGN_IN_REQUEST_CODE
import com.udacity.project4.utils.PreferencesManager

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var viewModel: AuthenticationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        viewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java] // No need for injection here

        if (PreferencesManager.retrieve<UserData>("userData") != null) {
            startActivity(Intent(this, RemindersActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        } else {
            initializeObservables()
        }

        binding.login.setOnClickListener {
            startLoginFlow()
        }

    }

    private fun initializeObservables() {
        viewModel.authenticationState.observe(this) { authenticationState ->
            when (authenticationState) {
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    binding.login.visibility = View.GONE
                    binding.loggedInUser.text = resources.getString(
                        R.string.logged_in_user,
                        FirebaseAuth.getInstance().currentUser?.displayName
                    )
                    binding.loggedInUser.visibility = View.VISIBLE
                    Handler().postDelayed({
                        startActivity(Intent(this, RemindersActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish() // Finish current AuthenticationActivity
                    }, 3000)
                }
                else -> {
                    binding.login.visibility = View.VISIBLE
                    binding.loggedInUser.visibility = View.GONE
                }
            }
        }
    }

    private fun startLoginFlow() {
        val customLayout = AuthMethodPickerLayout.Builder(R.layout.activity_authentication_login)
            .setEmailButtonId(R.id.login_with_email)
            .setGoogleButtonId(R.id.login_with_google)
            .build()

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setAuthMethodPickerLayout(customLayout)
                .setTheme(R.style.TodoReminderTheme)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }
}