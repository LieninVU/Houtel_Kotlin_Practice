package com.example.hotel_app.presentation.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hotel_app.R
import com.example.hotel_app.databinding.FragmentMapsBinding
import com.example.hotel_app.domain.model.RestaurantMarker
import com.example.hotel_app.presentation.viewmodel.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback {

    private val viewModel: MapsViewModel by viewModel()
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    
    private var googleMap: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMapsBinding.bind(view)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupListeners()
        observeState()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Add hotel marker
        val hotelLocation = LatLng(viewModel.hotelLocation.latitude, viewModel.hotelLocation.longitude)
        map.addMarker(
            MarkerOptions()
                .position(hotelLocation)
                .title(getString(R.string.maps_hotel_marker_title))
                .snippet(getString(R.string.maps_hotel_marker_snippet))
        )

        // Add restaurant markers
        viewModel.markers.value.forEach { marker ->
            val position = LatLng(marker.coordinates.latitude, marker.coordinates.longitude)
            map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(marker.name)
                    .snippet(marker.getSnippet())
            )
        }

        // Move camera to hotel location
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(hotelLocation, 14f))

        // Set map click listener
        map.setOnMarkerClickListener { marker ->
            val clickedMarker = viewModel.markers.value.find {
                it.coordinates.latitude == marker.position.latitude &&
                it.coordinates.longitude == marker.position.longitude
            }
            clickedMarker?.let {
                viewModel.selectMarker(it)
            }
            false
        }
    }

    private fun setupListeners() {
        binding.btnRoute.setOnClickListener {
            viewModel.buildRouteToRestaurant()
        }

        binding.btnCall.setOnClickListener {
            viewModel.callRestaurant()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedMarker.collect { marker ->
                        marker?.let {
                            binding.selectedRestaurantInfo.isVisible = true
                            binding.tvSelectedRestaurantName.text = it.name
                            binding.tvSelectedRestaurantCuisine.text = it.cuisine
                            binding.tvSelectedRestaurantRating.text = getString(R.string.maps_restaurant_rating_format, it.rating)
                            binding.tvSelectedRestaurantDistance.text = getString(R.string.maps_restaurant_distance_format, it.distance)
                            binding.tvSelectedRestaurantAddress.text = it.address
                        } ?: run {
                            binding.selectedRestaurantInfo.isVisible = false
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        googleMap = null
    }
}
