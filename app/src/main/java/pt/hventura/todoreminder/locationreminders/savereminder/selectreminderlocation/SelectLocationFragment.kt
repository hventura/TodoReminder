package pt.hventura.todoreminder.locationreminders.savereminder.selectreminderlocation

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.android.ext.android.inject
import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.base.BaseFragment
import pt.hventura.todoreminder.databinding.FragmentSelectLocationBinding
import pt.hventura.todoreminder.locationreminders.savereminder.SaveReminderViewModel
import pt.hventura.todoreminder.utils.setDisplayHomeAsUpEnabled
import java.io.FileOutputStream
import java.util.*

class SelectLocationFragment : BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val mViewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        map.setPadding(5, 0, 5, 70)
        setMapStyle(map)
        setOnPoiClick(map)
        setOnMapClick(map)
        val coimbra = LatLng(40.203348, -8.410291)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coimbra, 14F))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_select_location,
            container,
            false
        )

        binding.viewModel = mViewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // TODO: add the map setup implementation
        // TODO: zoom to the user location after taking his permission
        // TODO: add style to the map
        // TODO: put a marker to location that the user selected

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun onLocationSelected() {
        // TODO: When the user confirms on the selected location,
        //  send back the selected location details to the view model
        //  and navigate back to the previous fragment to save the reminder and add the geofence
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

    /**
     * Ensures that the style is applied and changed accordingly with hour of day
     * This logic can be enhanced given the TimeZone and/or through User configuration
     * For now lets keep it simple and say that from 19h forward is night time :P
     **/
    private fun setMapStyle(map: GoogleMap) {
        mViewModel.hourOfDay.observe(this.viewLifecycleOwner) { hour ->
            try {
                val successMapStyle = if (hour < 19) {
                    map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_day))
                } else {
                    map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_night))
                }

                if (!successMapStyle) {
                    mViewModel.showErrorMessage.value = "Something went wrong with the Map Style. Contact support and provide this error"
                }

            } catch (e: Resources.NotFoundException) {
                mViewModel.showErrorMessage.value = "Can't find the style. Error: ${e.message}"
            }
        }
    }

    private fun setOnPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            mViewModel.selectedLocation.value = null
            mViewModel.selectedPOI.value = poi
            val selectedPoi = map.addMarker(
                MarkerOptions().position(poi.latLng)
                    .title(poi.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, 16f), object : GoogleMap.CancelableCallback {
                override fun onCancel() {}

                override fun onFinish() {
                    captureMapScreen()
                    selectedPoi?.showInfoWindow()
                }
            })
        }
    }

    private fun setOnMapClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            map.clear()
            mViewModel.selectedLocation.value = latLng
            mViewModel.selectedPOI.value = null
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1.5f, Long: %2.5f",
                latLng.latitude, latLng.longitude
            )
            val selectedLocation = map.addMarker(
                MarkerOptions().position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f), object : GoogleMap.CancelableCallback {
                override fun onCancel() {}

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
                    val packageDir: String = packageInfo.applicationInfo.dataDir
                    mViewModel.reminderSnapshotLocation.value = packageDir + "/" + System.currentTimeMillis() + ".png"

                    val out = FileOutputStream(mViewModel.reminderSnapshotLocation.value)
                    bitmap!!.compress(Bitmap.CompressFormat.PNG, 90, out)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        map.snapshot(callback)
    }

}