package se.fork.bgrreading.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import se.fork.bgrreading.data.db.LinearAcceleration
import se.fork.bgrreading.data.db.LinearAccelerationDao
import se.fork.bgrreading.data.db.RotationVector
import se.fork.bgrreading.data.db.RotationVectorDao

private const val DATABASE_NAME = "bgr_reading_database"

@Database(entities = arrayOf(MyLocationEntity::class, LinearAcceleration::class, RotationVector::class), version = 3)
@TypeConverters(MyLocationTypeConverters::class)
abstract class BgrReadingDatabase : RoomDatabase() {
    abstract fun locationDao(): MyLocationDao
    abstract fun linearAcceleretionDao(): LinearAccelerationDao
    abstract fun rotationVectorDao(): RotationVectorDao

    companion object {
        // For Singleton instantiation
        @Volatile private var INSTANCE: BgrReadingDatabase? = null

        fun getInstance(context: Context): BgrReadingDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): BgrReadingDatabase {
            return Room.databaseBuilder(
                    context,
                    BgrReadingDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
