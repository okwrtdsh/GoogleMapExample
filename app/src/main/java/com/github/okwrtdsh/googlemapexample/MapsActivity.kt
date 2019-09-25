package com.github.okwrtdsh.googlemapexample

import android.content.Context
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlin.random.Random


class MyItem(position: LatLng, title: String, snippet: String) : ClusterItem {
    private val mPosition = position
    private val mTitle = title
    private val mSnippet = snippet

    override fun getSnippet() = mSnippet
    override fun getPosition() = mPosition
    override fun getTitle() = mTitle
}

class MyItemClusterRenderer(context: Context, map: GoogleMap, manager: ClusterManager<MyItem>) :
    DefaultClusterRenderer<MyItem>(context, map, manager) {
    override fun shouldRenderAsCluster(cluster: Cluster<MyItem>?): Boolean {
        return cluster?.size ?: 0 >= 5
    }
}

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var currentMarker: Marker? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mClusterManager: ClusterManager<MyItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mClusterManager = ClusterManager<MyItem>(this, mMap).apply {
            renderer = MyItemClusterRenderer(this@MapsActivity, mMap, this)
        }
        mMap.apply {
            setOnCameraIdleListener(mClusterManager)
            setOnMarkerClickListener(mClusterManager)
            isMyLocationEnabled = true
            uiSettings.apply {
                isScrollGesturesEnabled = true
                isZoomControlsEnabled = true
                isZoomGesturesEnabled = true
                isRotateGesturesEnabled = true
                isZoomGesturesEnabled = true
                isMapToolbarEnabled = true
                isTiltGesturesEnabled = true
                isCompassEnabled = true
                isMyLocationButtonEnabled = true
            }
        }

        // Add a marker in OsakaUniv and move the camera
        var latlng = LatLng(34.822014, 135.524468)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latlng = LatLng(location.latitude, location.longitude)
                    setCurrentMarker(latlng)
                }
            }
        setCurrentMarker(latlng)

        val rnd = Random
        (1..100).map {
            val ll = LatLng(
                rnd.nextDoubleNorm(latlng.latitude, 0.5),
                rnd.nextDoubleNorm(latlng.longitude)
            )
            mClusterManager.addItem(
                MyItem(
                    ll,
                    "point",
                    ll.toStr()
                )
            )
        }
    }

    private fun setCurrentMarker(latlon: LatLng) {
        currentMarker?.remove()
        currentMarker = mMap.addMarker(
            MarkerOptions()
                .position(latlon)
                .title("Your Location")
                .snippet(latlon.toStr())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlon, 14f))
    }
}

fun LatLng.toStr(): String {
    val latDeg: Int = latitude.toInt()
    val latMin: Int = ((latitude - latDeg.toDouble()) * 60.0).toInt()
    val latSec: Int = ((latitude - latDeg.toDouble() - latMin.toDouble() / 60.0) * 3600.0).toInt()
    val lngDeg: Int = longitude.toInt()
    val lngMin: Int = ((longitude - lngDeg.toDouble()) * 60.0).toInt()
    val lngSec: Int = ((longitude - lngDeg.toDouble() - lngMin.toDouble() / 60.0) * 3600.0).toInt()
    return "lat: %02d°%02d′%02d″, lng: %03d°%02d′%02d″".format(
        latDeg, latMin, latSec,
        lngDeg, lngMin, lngSec
    )
}

fun Random.nextDoubleNorm(mu: Double = 0.0, sigma: Double = 1.0): Double =
    ((1..12).map { nextDouble() }.sum() - 6.0) * sigma + mu

