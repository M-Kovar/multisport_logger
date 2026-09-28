package com.example.multisport_logger.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // ISO format: YYYY-MM-DD or YYYY-MM-DDTHH:mm:ss
    val venueName: String,
    val activityName: String,
    val cost: Double
)
