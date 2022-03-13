package com.udacity.project4.utils

object Constants {
    // AuthenticationActivity
    const val SIGN_IN_REQUEST_CODE = 1001

    // SelectLocationFragment - For user location only FINE LOCATION is needed
    const val REQUEST_LOCATION_PERMISSION = 2001
    const val INTERVAL = 0L
    const val FAST_INTERVAL = 0L

    // SaveReminderFragment - For save and access geofencing BACKGROUND LOCATION IS NEEDED
    val runningQOrLater = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 2002
    const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 2003
    const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 2004
    const val ACTION_GEOFENCE_EVENT = "geofenceReminder.action.ACTION_GEOFENCE_EVENT"
    const val GEOFENCE_RADIUS_METERS = 100F

}