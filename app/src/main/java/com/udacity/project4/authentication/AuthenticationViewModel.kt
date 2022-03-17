package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.map
import com.udacity.project4.authentication.data.UserData
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.utils.PreferencesManager

class AuthenticationViewModel(app: Application) : BaseViewModel(app) {

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            /**
             * For future use, Just to not save the FirebaseUser object.
             */
            val userData = UserData(
                user.displayName!!,
                user.email!!
            )
            PreferencesManager.put(userData, "userData")
            AuthenticationState.AUTHENTICATED
        } else {
            PreferencesManager.put(null, "userData")
            AuthenticationState.UNAUTHENTICATED
        }
    }

}