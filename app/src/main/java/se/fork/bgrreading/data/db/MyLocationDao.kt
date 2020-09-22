package se.fork.bgrreading.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Single
import java.util.UUID

/**
 * Defines database operations.
 */
@Dao
interface MyLocationDao {

    @Query("SELECT * FROM my_location_table ORDER BY timestamp DESC")
    fun getLocations(): Single<List<MyLocationEntity>>

    @Query("SELECT * FROM my_location_table WHERE id=(:id)")
    fun getLocation(id: UUID): Single<MyLocationEntity>

    @Query("delete from my_location_table")
    fun clearTable()

    @Update
    fun updateLocation(myLocationEntity: MyLocationEntity)

    @Insert
    fun addLocation(myLocationEntity: MyLocationEntity)

    @Insert
    fun addLocations(myLocationEntities: List<MyLocationEntity>)
}
