package pt.hventura.todoreminder.authentication

import android.app.Application
import androidx.lifecycle.map
import pt.hventura.todoreminder.authentication.data.UserData
import pt.hventura.todoreminder.base.BaseViewModel
import pt.hventura.todoreminder.utils.PreferencesManager

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
            AuthenticationState.UNAUTHENTICATED
        }
    }

}