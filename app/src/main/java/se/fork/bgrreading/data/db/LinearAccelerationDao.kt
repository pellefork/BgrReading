package se.fork.bgrreading.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Single

@Dao
interface LinearAccelerationDao {

    @Query("delete from linear_acceleration")
    fun clearTable()

    @Insert
    fun addAcceleration(acc : LinearAcceleration)

    @Query("select * from linear_acceleration order by timestamp")
    fun getAccelerations() : Single<List<LinearAcceleration>>

    @Query("select * from linear_acceleration where timestamp between :beginTimestamp and :endTimestamp order by timestamp")
    fun getAccelerations(beginTimestamp: Long, endTimestamp: Long) : Single<List<LinearAcceleration>>

    @Query("select count(*) from linear_acceleration")
    fun getCount() : Single<Long>

}