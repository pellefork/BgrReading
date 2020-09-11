package se.fork.bgrreading

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.content_main.*
import se.fork.bgrreading.data.db.BgrReadingRepository
import se.fork.bgrreading.extensions.launchActivity
import se.fork.bgrreading.extensions.onClickWithDebounce
import timber.log.Timber
import java.util.concurrent.Executors

private const val RC_SIGN_IN = 4711

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_FOREGROUND = 1544
        const val REQUEST_CODE_BACKGROUND = 1545
        val repo = BgrReadingApplication.bgrReadingRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        setupClickListeners()
        requestPermission()
        requestBackgroundPermission()
        authenticate()
    }

    private fun authenticate() {
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                Toast.makeText(this, "Inloggad som $user", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Inloggningen sket sig", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        clear_button.onClickWithDebounce {
            Timber.d("clearDatabase")
            repo.clearDatabase()
        }
        start_location_button.onClickWithDebounce {
            Timber.d("startLocationUpdates")
            repo.startLocationUpdates()
        }
        stop_location_button.onClickWithDebounce {
            Timber.d("stopLocationUpdates")
            repo.stopLocationUpdates()
        }
        start_motion_button.onClickWithDebounce {
            Timber.d("startLocationUpdates")
            repo.startMotionSensorUpdates()
        }
        stop_motion_button.onClickWithDebounce {
            Timber.d("stopLocationUpdates")
            repo.stopMotionSensorUpdates()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_colocation -> {
                launchActivity<ColocationActivity> {  }
                true
            }
            R.id.action_test_firebase -> {
                BgrReadingRepository.getInstance(this,  Executors.newSingleThreadExecutor()).testFirebase()
                true
            }
            R.id.action_upload_session -> {
                BgrReadingRepository.getInstance(this,  Executors.newSingleThreadExecutor()).buildAndUploadSession(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_FOREGROUND -> handlePermissionForForeground()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestPermission() {
        val hasLocationPermission = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission) {
            // handle location update
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_FOREGROUND)
        }
    }

    private fun requestBackgroundPermission() {
        val hasForegroundLocationPermission = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasForegroundLocationPermission) {
            val hasBackgroundLocationPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (hasBackgroundLocationPermission) {
                // handle location update
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE_BACKGROUND)
            }
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE_BACKGROUND)
        }
    }

    private fun handlePermissionForForeground() {

    }
}