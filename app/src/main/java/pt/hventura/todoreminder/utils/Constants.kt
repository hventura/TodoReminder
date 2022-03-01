package pt.hventura.todoreminder.utils

object Constants {
    const val SIGN_IN_REQUEST_CODE = 1001
    const val REQUEST_LOCATION_PERMISSION = 2001
    const val REQUEST_GPS_PERMISSION = 2002
    const val ACTION_GEOFENCE_EVENT = "SaveReminderFragment.todoreminder.action.ACTION_GEOFENCE_EVENT"
    const val INTERVAL = 5000L
    const val FAST_INTERVAL = 2000L
    const val MIN_DISTANCE = 100F
    val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
}