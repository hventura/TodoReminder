package pt.hventura.todoreminder.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.databinding.ActivityAuthenticationBinding
import timber.log.Timber

class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val SIGN_IN_REQUEST_CODE = 1001
    }

    private lateinit var binding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        binding.login.setOnClickListener {
            startLoginFlow()
        }

        /*
            TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

            TODO: If the user was authenticated, send him to RemindersActivity

            TODO: a bonus is to customize the sign in flow to look nice using :
            https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
        */

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Timber.i("Successfully Logged in user :)")
            } else {
                Timber.i("Logged in error :(")
            }
        }
    }

    private fun startLoginFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_REQUEST_CODE
        )
    }
}