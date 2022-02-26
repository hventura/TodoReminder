package pt.hventura.todoreminder.utils

import com.google.android.gms.maps.model.LatLng

object Constants {

    const val SIGN_IN_REQUEST_CODE = 1001
    const val REQUEST_FORE_AND_BACKGROUND_PERMISSION_CODE = 2001
    const val REQUEST_FOREGROUND_PERMISSION_CODE = 2002
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 2003
    const val REQUEST_LOCATION_PERMISSION = 2004
    const val REQUEST_GPS_PERMISSION = 2005
    const val INTERVAL = 5000L
    const val FAST_INTERVAL = 2000L
    const val MIN_DISTANCE = 1000F
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    val COIMBRA = LatLng(40.203348, -8.410291)

}