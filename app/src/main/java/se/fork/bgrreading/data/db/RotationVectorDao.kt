package se.fork.bgrreading.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Single

@Dao
interface RotationVectorDao {

    @Query("delete from rotation_vector")
    fun clearTable()

    @Insert
    fun addRotationVector(vector : RotationVector)

    @Query("select * from rotation_vector order by timestamp")
    fun getRotationVectors() : Single<List<RotationVector>>

    @Query("select * from rotation_vector where timestamp between :beginTimestamp and :endTimestamp order by timestamp")
    fun getRotationVectors(beginTimestamp: Long, endTimestamp: Long) : Single<List<RotationVector>>

    @Query("select count(*) from rotation_vector")
    fun getCount() : Single<Long>

}