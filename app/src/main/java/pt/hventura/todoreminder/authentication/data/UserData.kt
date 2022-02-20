package pt.hventura.todoreminder.authentication.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Data class just to not save the FirebaseUser object.
/**
 * Parcelize -> why i love kotlin :)
 * Instructs the Kotlin compiler to generate
 * writeToParcel() ans describeContents() Parcelable methods,
 * as well as a CREATOR factory class <b>automatically</b>.
 */
@Parcelize
data class UserData(
    val userName: String,
    val userEmail: String,
) : Parcelable
