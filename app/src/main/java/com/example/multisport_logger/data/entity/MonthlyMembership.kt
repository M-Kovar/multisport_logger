package com.example.multisport_logger.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "monthly_memberships")
data class MonthlyMembership(
    @PrimaryKey
    val yearMonth: String, // e.g. "2025-05"
    val cardCost: Double
)
