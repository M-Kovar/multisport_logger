package com.example.multisport_logger.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.multisport_logger.data.dao.ActivityDao
import com.example.multisport_logger.data.dao.MembershipDao
import com.example.multisport_logger.data.dao.TemplateDao
import com.example.multisport_logger.data.entity.ActivityLog
import com.example.multisport_logger.data.entity.ActivityTemplate
import com.example.multisport_logger.data.entity.MonthlyMembership

@Database(
    entities = [
        ActivityLog::class,
        ActivityTemplate::class,
        MonthlyMembership::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun templateDao(): TemplateDao
    abstract fun membershipDao(): MembershipDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "multisport_logger_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
