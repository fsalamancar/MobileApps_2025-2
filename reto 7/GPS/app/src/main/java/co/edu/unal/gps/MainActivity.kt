package co.edu.unal.gps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null

    private lateinit var poiRecyclerView: RecyclerView
    private lateinit var poiAdapter: PoiAdapter
    private val poiItems = mutableListOf<PoiItem>()
    private lateinit var locationLoadingIndicator: LinearLayout
    private lateinit var searchView: SearchView
    private lateinit var placesClient: PlacesClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 3000
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_api_key))
        }
        placesClient = Places.createClient(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize RecyclerView
        poiRecyclerView = findViewById(R.id.poi_list)
        poiRecyclerView.layoutManager = LinearLayoutManager(this)
        poiAdapter = PoiAdapter(poiItems)
        poiRecyclerView.adapter = poiAdapter

        locationLoadingIndicator = findViewById(R.id.location_loading_indicator)
        searchView = findViewById(R.id.search_view)

        setupSearchView()
        createLocationRequest()
        createLocationCallback()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchPlace(it)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Implement autocomplete suggestions here if desired
                return false
            }
        })
    }

    private fun searchPlace(query: String) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            if (response.autocompletePredictions.isNotEmpty()) {
                val firstPrediction = response.autocompletePredictions.first()
                val placeId = firstPrediction.placeId

                val fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener { fetchResponse ->
                    val place = fetchResponse.place
                    place.latLng?.let { latLng ->
                        googleMap.clear()
                        googleMap.addMarker(MarkerOptions().position(latLng).title(place.name))
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        // Optionally, find nearby places around this searched location
                        // findNearbyPlaces(latLng)
                    }
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "Place not found: ${exception.message}")
                    Toast.makeText(this, "Lugar no encontrado: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No se encontraron resultados para '$query'", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Autocomplete failed: ${exception.message}")
            Toast.makeText(this, "Búsqueda fallida: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_IN_MILLISECONDS)
            .setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
            .build()
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let {
                    currentLocation = it
                    updateMapAndPois(it)
                    stopLocationUpdates() // Stop updates once location is found
                    locationLoadingIndicator.visibility = View.GONE
                } ?: run {
                    // Location is still null, keep showing indicator
                    locationLoadingIndicator.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL // Set map type to normal for detailed view
        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            locationLoadingIndicator.visibility = View.VISIBLE // Show indicator when trying to get location

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    updateMapAndPois(location)
                    locationLoadingIndicator.visibility = View.GONE // Hide indicator once location is found
                } else {
                    // Last location is null, start active location updates
                    startLocationUpdates()
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateMapAndPois(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)
        googleMap.clear() // Clear existing markers
        googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
        findNearbyPlaces(currentLatLng)
    }

    private fun findNearbyPlaces(currentLatLng: LatLng) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val searchRadius = sharedPreferences.getString("search_radius", "5")?.toIntOrNull() ?: 5
        Log.d(TAG, "Search Radius: $searchRadius km")

        val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG)

        val request = FindCurrentPlaceRequest.builder(placeFields)
            .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    googleMap.clear() // Clear existing markers
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location")) // Re-add current location marker
                    val newPoiItems = mutableListOf<PoiItem>()
                    for (placeLikelihood in response?.placeLikelihoods ?: emptyList()) {
                        val place = placeLikelihood.place
                        place.latLng?.let { placeLatLng ->
                            val distance = calculateDistance(currentLatLng, placeLatLng)
                            Log.d(TAG, "Place: ${place.name}, Distance: ${String.format("%.2f", distance)} km")
                            if (distance <= searchRadius) {
                                googleMap.addMarker(MarkerOptions().position(placeLatLng).title(place.name))
                                newPoiItems.add(PoiItem(place, distance))
                            }
                        }
                    }
                    runOnUiThread { // Update UI on the main thread
                        poiItems.clear()
                        poiItems.addAll(newPoiItems.sortedBy { it.distance })
                        poiAdapter.notifyDataSetChanged()
                    }
                } else {
                    val exception = task.exception
                    if (exception != null) {
                        Toast.makeText(this, "Place search failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun calculateDistance(latLng1: LatLng, latLng2: LatLng): Double {
        val earthRadius = 6371.0 // kilometers
        val dLat = Math.toRadians(latLng2.latitude - latLng1.latitude)
        val dLon = Math.toRadians(latLng2.longitude - latLng1.longitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(latLng1.latitude)) * Math.cos(Math.toRadians(latLng2.latitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::googleMap.isInitialized) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startLocationUpdates()
                currentLocation?.let { updateMapAndPois(it) }
                if (currentLocation == null) {
                    locationLoadingIndicator.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }
}