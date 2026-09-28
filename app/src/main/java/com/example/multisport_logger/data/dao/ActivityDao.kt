package com.example.multisport_logger.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.multisport_logger.data.entity.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ActivityLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<ActivityLog>)

    @Update
    suspend fun update(log: ActivityLog)

    @Delete
    suspend fun delete(log: ActivityLog)

    @Query("DELETE FROM activity_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAll()

    @Query("SELECT * FROM activity_logs WHERE id = :id")
    suspend fun getById(id: Long): ActivityLog?

    @Query("SELECT * FROM activity_logs ORDER BY date DESC, id DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs ORDER BY date DESC, id DESC")
    suspend fun getAllLogsList(): List<ActivityLog>

    @Query("SELECT * FROM activity_logs WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, id DESC")
    fun getLogsByDateRange(startDate: String, endDate: String): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE date LIKE :yearMonth || '%' ORDER BY date DESC, id DESC")
    fun getLogsForMonth(yearMonth: String): Flow<List<ActivityLog>>

    @Query("SELECT COALESCE(SUM(cost), 0.0) FROM activity_logs WHERE date >= :startDate AND date <= :endDate")
    fun getSumCostForDateRange(startDate: String, endDate: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(cost), 0.0) FROM activity_logs WHERE date LIKE :yearMonth || '%'")
    fun getSumCostForMonth(yearMonth: String): Flow<Double>

    @Query("SELECT COALESCE(SUM(cost), 0.0) FROM activity_logs")
    fun getTotalSumCost(): Flow<Double>
}
