package se.fork.bgrreading

import android.annotation.SuppressLint
import android.location.Address
import android.location.Location
import androidx.lifecycle.*
import com.google.android.gms.location.LocationRequest
import com.patloew.colocation.CoGeocoder
import com.patloew.colocation.CoLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class ColocationViewModel (
    private val coLocation: CoLocation,
    private val coGeocoder: CoGeocoder
) : ViewModel(), LifecycleObserver {
    private val locationRequest: LocationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        //.setSmallestDisplacement(1f)
        //.setNumUpdates(3)
        .setInterval(0)
        .setFastestInterval(41)


    private val mutableLocationUpdates: MutableLiveData<Location> = MutableLiveData()
    val locationUpdates: LiveData<Location> = mutableLocationUpdates
    val addressUpdates: LiveData<Address?> = locationUpdates.switchMap { location ->
        liveData { emit(coGeocoder.getAddressFromLocation(location)) }
    }

    private val mutableResolveSettingsEvent: MutableLiveData<CoLocation.SettingsResult.Resolvable> = MutableLiveData()
    val resolveSettingsEvent: LiveData<CoLocation.SettingsResult.Resolvable> = mutableResolveSettingsEvent

    private var locationUpdatesJob: Job? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        startLocationUpdatesAfterCheck()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = null
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdatesAfterCheck() {
        viewModelScope.launch {
            val settingsResult = coLocation.checkLocationSettings(locationRequest)
            when (settingsResult) {
                CoLocation.SettingsResult.Satisfied -> {
                    coLocation.getLastLocation()?.run(mutableLocationUpdates::postValue)
                    startLocationUpdates()
                }
                is CoLocation.SettingsResult.Resolvable -> mutableResolveSettingsEvent.postValue(settingsResult)
                else -> { /* Ignore for now, we can't resolve this anyway */ }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = viewModelScope.launch {
            try {
                coLocation.getLocationUpdates(locationRequest).collect { location ->
                    Timber.d("Location update received: $location")
                    mutableLocationUpdates.postValue(location)
                }
            } catch (e: CancellationException) {
                Timber.e(e,"Location updates cancelled")
            }
        }
    }

}