package com.example.multisport_logger.data.repository

import com.example.multisport_logger.data.entity.ActivityLog
import com.example.multisport_logger.data.entity.ActivityTemplate
import com.example.multisport_logger.data.entity.MonthlyMembership
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface MultisportRepository {
    // Default Recurring Card Cost
    fun getDefaultCardCost(): Flow<Double>
    suspend fun getDefaultCardCostDirect(): Double
    suspend fun setDefaultCardCost(cost: Double)

    // Activity Logs
    fun getAllLogs(): Flow<List<ActivityLog>>
    suspend fun getAllLogsList(): List<ActivityLog>
    suspend fun getLogById(id: Long): ActivityLog?
    fun getLogsByDateRange(startDate: String, endDate: String): Flow<List<ActivityLog>>
    fun getLogsForMonth(yearMonth: String): Flow<List<ActivityLog>>
    fun getSumCostForDateRange(startDate: String, endDate: String): Flow<Double>
    fun getSumCostForMonth(yearMonth: String): Flow<Double>
    fun getTotalSumCost(): Flow<Double>
    suspend fun insertLog(log: ActivityLog): Long
    suspend fun insertAllLogs(logs: List<ActivityLog>)
    suspend fun updateLog(log: ActivityLog)
    suspend fun deleteLog(log: ActivityLog)
    suspend fun deleteLogById(id: Long)
    suspend fun deleteAllLogs()

    // Activity Templates
    fun getAllTemplates(): Flow<List<ActivityTemplate>>
    suspend fun getAllTemplatesList(): List<ActivityTemplate>
    suspend fun getTemplateById(id: Long): ActivityTemplate?
    suspend fun insertTemplate(template: ActivityTemplate): Long
    suspend fun insertAllTemplates(templates: List<ActivityTemplate>)
    suspend fun updateTemplate(template: ActivityTemplate)
    suspend fun deleteTemplate(template: ActivityTemplate)
    suspend fun deleteTemplateById(id: Long)
    suspend fun deleteAllTemplates()

    // Monthly Memberships
    fun getAllMemberships(): Flow<List<MonthlyMembership>>
    suspend fun getAllMembershipsList(): List<MonthlyMembership>
    fun getMembershipForMonth(yearMonth: String): Flow<MonthlyMembership>
    suspend fun getMembershipForMonthDirect(yearMonth: String): MonthlyMembership
    suspend fun ensureCurrentMonthMembership(yearMonth: String = YearMonth.now().toString()): MonthlyMembership
    suspend fun saveMembership(membership: MonthlyMembership)
    suspend fun insertAllMemberships(memberships: List<MonthlyMembership>)
    suspend fun deleteMembership(membership: MonthlyMembership)
    suspend fun deleteMembershipByYearMonth(yearMonth: String)
    suspend fun deleteAllMemberships()
}
