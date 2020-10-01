package se.fork.bgrreading

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import se.fork.bgrreading.adapters.SessionAdapter
import se.fork.bgrreading.adapters.SessionSwipeHelper
import se.fork.bgrreading.data.db.BgrReadingRepository
import se.fork.bgrreading.data.remote.Session
import se.fork.bgrreading.extensions.launchActivity
import se.fork.bgrreading.extensions.onClickWithDebounce
import timber.log.Timber
import java.util.concurrent.Executors

private const val RC_SIGN_IN = 4711

class MainActivity : AppCompatActivity(), SessionSwipeHelper.RecyclerItemTouchHelperListener {

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
        initializeRecycler()
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
                val name  = user?.displayName
                Toast.makeText(this, "Logged in as $name.", Toast.LENGTH_SHORT).show()
                fetchData()
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
        upload_session_button.onClickWithDebounce {
            Timber.d("stopLocationUpdates")
            repo.buildAndUploadSession(this, {
               onUploadSuccess()
            }, {
                onUploadError(it)
            })
        }

        fab.onClickWithDebounce {
            Timber.d("stopLocationUpdates")
            startStop()
        }
    }

    private fun onUploadSuccess() {
        Toast.makeText(this, "Session uploaded", Toast.LENGTH_SHORT).show()
    }

    private fun onUploadError(error: DatabaseError) {
        Toast.makeText(this, "Session upload error: ${error.message}", Toast.LENGTH_SHORT).show()
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
            R.id.action_toggle_buttons -> {
                toggleButtons()
                true
            }
            R.id.action_test_firebase -> {
                BgrReadingRepository.getInstance(this,  Executors.newSingleThreadExecutor()).testFirebase()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleButtons() {
        if (buttons.visibility.equals(View.VISIBLE)) {
            buttons.visibility = View.GONE
        } else {
            buttons.visibility = View.VISIBLE
        }
    }

    val receivingLocationUpdates: LiveData<Boolean> = repo.receivingLocationUpdates

    private fun startStop() {
        if (isRecording()) {
            fab.setImageResource(android.R.drawable.ic_input_add)
            stopRecording()
        } else {
            fab.setImageResource(android.R.drawable.ic_media_pause)
            startRecording()
        }

    }

    private fun startRecording() {
        Toast.makeText(this, "Starting recording", Toast.LENGTH_SHORT).show()
        repo.startLocationUpdates()
        repo.startMotionSensorUpdates()
    }

    private fun stopRecording() {
        Toast.makeText(this, "Stopping recording", Toast.LENGTH_SHORT).show()
        repo.stopLocationUpdates()
        repo.stopMotionSensorUpdates()
        repo.buildAndUploadSession(this, {
            repo.clearDatabase()
            onUploadSuccess()
        }, {
            onUploadError(it)
        })
    }

    private fun isRecording() : Boolean {
        return receivingLocationUpdates.value ?: false
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

    private fun fetchData() {
        Timber.d("fetchData")
        val query = FirebaseDatabase.getInstance()
            .reference
            .child("sessions")
            .limitToLast(50)

        val options = FirebaseRecyclerOptions.Builder<Session>()
            .setQuery(query, Session::class.java)
            .setLifecycleOwner(this)
            .build()

        val adapter = SessionAdapter(options)
        recycler.adapter = adapter
        adapter.startListening()
    }

    private fun initializeRecycler() {
        val gridLayoutManager = GridLayoutManager(this, 1)
        gridLayoutManager.orientation = RecyclerView.VERTICAL

        recycler.apply {
            layoutManager = gridLayoutManager
        }

        // Set up swipe to delete

        recycler.itemAnimator = DefaultItemAnimator()
        val itemCallback = SessionSwipeHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(itemCallback).attachToRecyclerView(recycler)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        val session = (recycler.adapter as SessionAdapter).getItem(position)
        val sessionId = session.id
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete?")
        builder.setMessage("Delete session ${session.name}?")
        builder.setPositiveButton("Yes") { dialog, which ->
            deleteSession(sessionId)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.cancel()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun deleteSession(id: String) {
        Toast.makeText(this, "Deleting $id", Toast.LENGTH_SHORT).show()
        repo.deleteSession(id)
    }
}