package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AppDao
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PaymentEntity
import com.example.data.local.entities.PreferenceEntity
import com.example.data.local.entities.SemesterEntity
import com.example.data.local.entities.CourseEntity
import com.example.data.local.entities.ScheduleEntity

@Database(
    entities = [
        FavoriteEntity::class, 
        PaymentEntity::class, 
        PreferenceEntity::class, 
        NotificationEntity::class,
        SemesterEntity::class,
        CourseEntity::class,
        ScheduleEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // P1.3: 3->4 no schema change — empty migration retains data (replaces destructive)
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // no-op — schema unchanged, just version bump for exportSchema
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jamaaty_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
