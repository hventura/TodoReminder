package pt.hventura.todoreminder.locationreminders.savereminder.selectreminderlocation

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.android.ext.android.inject
import pt.hventura.todoreminder.R
import pt.hventura.todoreminder.base.BaseFragment
import pt.hventura.todoreminder.databinding.FragmentSelectLocationBinding
import pt.hventura.todoreminder.locationreminders.savereminder.SaveReminderViewModel
import pt.hventura.todoreminder.utils.setDisplayHomeAsUpEnabled

class SelectLocationFragment : BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val mViewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

}