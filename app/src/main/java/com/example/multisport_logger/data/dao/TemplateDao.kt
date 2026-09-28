package com.example.multisport_logger.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.multisport_logger.data.entity.ActivityTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: ActivityTemplate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<ActivityTemplate>)

    @Update
    suspend fun update(template: ActivityTemplate)

    @Delete
    suspend fun delete(template: ActivityTemplate)

    @Query("DELETE FROM activity_templates WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM activity_templates")
    suspend fun deleteAll()

    @Query("SELECT * FROM activity_templates WHERE id = :id")
    suspend fun getById(id: Long): ActivityTemplate?

    @Query("SELECT * FROM activity_templates ORDER BY venueName ASC, activityName ASC")
    fun getAllTemplates(): Flow<List<ActivityTemplate>>

    @Query("SELECT * FROM activity_templates ORDER BY venueName ASC, activityName ASC")
    suspend fun getAllTemplatesList(): List<ActivityTemplate>
}
