package com.example.multisport_logger.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "activity_templates")
data class ActivityTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val venueName: String,
    val activityName: String,
    val defaultCost: Double
)
