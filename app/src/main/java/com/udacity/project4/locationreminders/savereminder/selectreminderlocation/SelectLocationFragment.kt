package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.Constants.FAST_INTERVAL
import com.udacity.project4.utils.Constants.INTERVAL
import com.udacity.project4.utils.Constants.REQUEST_LOCATION_PERMISSION
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.FileOutputStream

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    /*************
     * VARIABLES *
     *************/

    //Use Koin to get the view model of the SaveReminder
    override val viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private var snackbar: Snackbar? = null
    private var mapReady = false
    private var permissionsGranted = false
    private var selectedPoi: PointOfInterest? = null

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_select_location, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        checkPermissions()

        // DONE: 1) add the map setup implementation
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.confirmButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setPadding(5, 0, 5, 200)
        // DONE: 2) zoom to the user location after taking his permission
        if (permissionsGranted) {
            enableMyLocation(map)
        }
        // DONE: 3) add style to the map
        setMapStyle(map)
        // DONE: 4) put a marker to location that the user selected
        setOnPoiClick(map)
        setOnMapClick(map)
        mapReady = true
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

    /*************
     * FUNCTIONS *
     *************/

    @SuppressLint("MissingPermission")
    private fun enableMyLocation(map: GoogleMap) {
        map.isMyLocationEnabled = true
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = INTERVAL
            fastestInterval = FAST_INTERVAL
        }
        LocationServices.getFusedLocationProviderClient(requireContext()).requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                LocationServices.getFusedLocationProviderClient(requireContext()).removeLocationUpdates(this)
                if (locationResult.locations.size > 0) {
                    val latLng = LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
                    val cameraPosition = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(18f)
                        .bearing(locationResult.lastLocation.bearing)
                        .build()
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                }
                super.onLocationResult(locationResult)
            }
        }, Looper.getMainLooper())
    }

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

    /***************
     * PERMISSIONS *
     ***************/
    private fun checkPermissions() {
        if (isPermissionGranted()) {
            permissionsGranted = true
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                permissionsGranted = true
                binding.confirmButton.isEnabled = true
                if (mapReady) {
                    enableMyLocation(map)
                }
            } else {
                binding.confirmButton.isEnabled = false

                snackbar = Snackbar.make(
                    requireView(),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }
                snackbar!!.show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (snackbar != null && snackbar!!.isShown) {
            snackbar!!.dismiss()
        }
    }

}