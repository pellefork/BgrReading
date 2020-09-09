package se.fork.bgrreading

import android.app.Activity
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.patloew.colocation.CoGeocoder
import com.patloew.colocation.CoLocation
import timber.log.Timber
import java.text.DateFormat
import java.util.*

class ColocationActivity: AppCompatActivity() {

    companion object {
        private val DATE_FORMAT = DateFormat.getDateTimeInstance()
        private const val REQUEST_SHOW_SETTINGS = 123
    }

    private var lastUpdate: TextView? = null
    private var locationText: TextView? = null
    private var addressText: TextView? = null

    private val viewModel: ColocationViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                ColocationViewModel(
                    CoLocation.from(this@ColocationActivity),
                    CoGeocoder.from(this@ColocationActivity)
                ) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_colocation)

        lastUpdate = findViewById(R.id.tv_last_update)
        locationText = findViewById(R.id.tv_current_location)
        addressText = findViewById(R.id.tv_current_address)
        lifecycle.addObserver(viewModel)
        viewModel.locationUpdates.observe(this, this::onLocationUpdate)
        viewModel.addressUpdates.observe(this, this::onAddressUpdate)
        viewModel.resolveSettingsEvent.observe(this) { it.resolve(this, REQUEST_SHOW_SETTINGS) }
    }

    override fun onResume() {
        super.onResume()
        checkPlayServicesAvailable()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SHOW_SETTINGS && resultCode == Activity.RESULT_OK) viewModel.startLocationUpdates()
    }

    private fun checkPlayServicesAvailable() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val status = apiAvailability.isGooglePlayServicesAvailable(this)

        if (status != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(status)) {
                apiAvailability.getErrorDialog(this, status, 1).show()
            } else {
                Snackbar.make(
                    lastUpdate!!,
                    "Google Play Services unavailable. This app will not work",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }
    }

    private fun onLocationUpdate(location: Location?) {
        Timber.d("onLocationUpdate: $location")
        lastUpdate!!.text = DATE_FORMAT.format(Date())
        locationText!!.text = location?.run { "$latitude, $longitude" } ?: "N/A"
    }

    private fun onAddressUpdate(address: Address?) {
        Timber.d("onAddressUpdate: ${address?.fullText}")
        addressText!!.text = address?.fullText ?: "N/A"
    }

    private val Address.fullText: String
        get() = (0..maxAddressLineIndex).joinToString(separator = "\n") { getAddressLine(it) }
}