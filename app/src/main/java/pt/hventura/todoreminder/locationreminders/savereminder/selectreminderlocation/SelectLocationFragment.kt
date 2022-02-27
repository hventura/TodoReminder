package pt.hventura.todoreminder.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import org.koin.android.ext.android.inject
import org.koin.core.component.getScopeName
import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.base.BaseFragment
import pt.hventura.todoreminder.base.NavigationCommand
import pt.hventura.todoreminder.databinding.FragmentSelectLocationBinding
import pt.hventura.todoreminder.locationreminders.savereminder.SaveReminderViewModel
import pt.hventura.todoreminder.utils.Constants.FAST_INTERVAL
import pt.hventura.todoreminder.utils.Constants.INTERVAL
import pt.hventura.todoreminder.utils.Constants.REQUEST_GPS_PERMISSION
import pt.hventura.todoreminder.utils.Constants.REQUEST_LOCATION_PERMISSION
import pt.hventura.todoreminder.utils.setDisplayHomeAsUpEnabled
import timber.log.Timber
import java.io.FileOutputStream


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, LocationListener {
    /**
     * VARIABLES
     * */
    //Use Koin to get the view model of the SaveReminder
    override val viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private var selectedPoi: PointOfInterest? = null

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = INTERVAL
        locationRequest.fastestInterval = FAST_INTERVAL

        // TODO: 1) add the map setup implementation
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.confirmButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setPadding(5, 0, 5, 70)
        // TODO: 2) zoom to the user location after taking his permission
        enableMyLocation()
        // TODO: 3) add style to the map
        setMapStyle(map)
        // TODO: 4) put a marker to location that the user selected
        setOnPoiClick(map)
        setOnMapClick(map)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_map -> {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.hybrid_map -> {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            R.id.satellite_map -> {
                map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_map -> {
                map.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onLocationChanged(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18f)
        map.animateCamera(cameraUpdate)
    }

    /**
     * FUNCTIONS
     * */

    private fun onLocationSelected() {
        if (selectedPoi != null) {
            viewModel.selectedPOI.value = selectedPoi
            viewModel.reminderSelectedLocationStr.value = selectedPoi!!.name
            viewModel.latitude.value = selectedPoi!!.latLng.latitude
            viewModel.longitude.value = selectedPoi!!.latLng.longitude
            viewModel.navigationCommand.value = NavigationCommand.Back
        } else {
            viewModel.showSnackBar.value = "You did not choose a location!"
        }

    }

    private fun setMapStyle(map: GoogleMap) {
        /**
         * Ensures that the style is applied and changed accordingly with hour of day
         * This logic can be enhanced given the TimeZone and/or through User configuration
         * For now lets keep it simple and say that from 19h forward is night time :P
         **/
        viewModel.hourOfDay.observe(this.viewLifecycleOwner) { hour ->
            try {
                val successMapStyle = if (hour < 19) {
                    map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_day))
                } else {
                    map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_night))
                }

                if (!successMapStyle) {
                    viewModel.showErrorMessage.value = "Something went wrong with the Map Style. Contact support and provide this error"
                }

            } catch (e: Resources.NotFoundException) {
                viewModel.showErrorMessage.value = "Can't find the style. Error: ${e.message}"
            }
        }
    }

    private fun setOnPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            selectedPoi = poi
            val customPOI = map.addMarker(
                MarkerOptions().position(poi.latLng)
                    .title(poi.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, 18f), object : GoogleMap.CancelableCallback {
                override fun onCancel() = Unit

                override fun onFinish() {
                    customPOI?.showInfoWindow()
                    captureMapScreen()
                }
            })
        }
    }

    private fun setOnMapClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            map.clear()
            val snippet = getString(R.string.lat_long_snippet, latLng.latitude, latLng.longitude)
            val poiName = getString(R.string.lat_long_title, latLng.latitude, latLng.longitude)
            val selectedLocation = map.addMarker(
                MarkerOptions().position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            selectedPoi = PointOfInterest(latLng, poiName, poiName)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f), object : GoogleMap.CancelableCallback {
                override fun onCancel() = Unit

                override fun onFinish() {
                    captureMapScreen()
                    selectedLocation!!.showInfoWindow()
                }
            })
        }
    }

    private fun captureMapScreen() {
        val callback: SnapshotReadyCallback = object : SnapshotReadyCallback {
            var bitmap: Bitmap? = null
            override fun onSnapshotReady(snapshot: Bitmap?) {
                bitmap = snapshot
                try {
                    // https://stackoverflow.com/questions/5527764/get-application-directory
                    val packageManager: PackageManager = requireActivity().packageManager
                    val packageName: String = requireActivity().packageName
                    val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
                    val packageDir: String = packageInfo.applicationInfo.dataDir + "/" + System.currentTimeMillis() + ".png"
                    val out = FileOutputStream(packageDir)
                    bitmap!!.compress(Bitmap.CompressFormat.PNG, 50, out)
                    viewModel.reminderSnapshotLocation.value = packageDir
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        map.snapshot(callback)
    }

    private fun isPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            if (isGPSEnabled()) {
                LocationServices.getFusedLocationProviderClient(requireContext()).requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        LocationServices.getFusedLocationProviderClient(requireContext())
                            .removeLocationUpdates(this)
                        if (locationResult.locations.size > 0) {
                            val latLng = LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
                            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18f)
                            map.animateCamera(cameraUpdate)
                        }
                    }
                }, Looper.getMainLooper())
            } else {
                turnOnGPS()
            }
        } else {
            map.isMyLocationEnabled = false
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun isGPSEnabled(): Boolean {
        var isEnable = false
        if (isPermissionGranted()) {
            val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
        return isEnable
    }

    private fun turnOnGPS() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(requireContext())
            .checkLocationSettings(builder.build())
        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                viewModel.showSnackBar.value = "GPS is already turned on"
            } catch (err: ApiException) {
                when (err.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            val resolvableApiException = err as ResolvableApiException
                            resolvableApiException.startResolutionForResult(requireActivity(), REQUEST_GPS_PERMISSION)
                        } catch (ex: SendIntentException) {
                            ex.printStackTrace()
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }
}