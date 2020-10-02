package se.fork.bgrreading.data.remote

import java.io.Serializable
import java.util.*

data class SessionHeader(
    val id : String = "",
    var name: String = "",
    var uploadDate: Date = Date(),
    var startDate: Date = Date(),
    var endDate: Date = Date(),
    var deviceId: String? = null,
    var deviceName: String? = null,
    var userName: String? = null,

    var nLocations : Int? = null,
    var nAccelerations : Int? = null,
    var nRotations : Int? = null
    ) : Serializable {
}