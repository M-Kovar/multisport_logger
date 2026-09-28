package com.example.multisport_logger

import com.example.multisport_logger.data.entity.ActivityLog
import com.example.multisport_logger.data.entity.ActivityTemplate
import com.example.multisport_logger.data.entity.MonthlyMembership
import com.example.multisport_logger.data.repository.MultisportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FakeMultisportRepository(
    initialDefaultCost: Double = 500.0
) : MultisportRepository {

    private val defaultCostFlow = MutableStateFlow(initialDefaultCost)
    private val logsMap = mutableMapOf<Long, ActivityLog>()
    private val templatesMap = mutableMapOf<Long, ActivityTemplate>()
    private val membershipsMap = mutableMapOf<String, MonthlyMembership>()

    private val logsFlow = MutableStateFlow<List<ActivityLog>>(emptyList())
    private val templatesFlow = MutableStateFlow<List<ActivityTemplate>>(emptyList())
    private val membershipsFlow = MutableStateFlow<List<MonthlyMembership>>(emptyList())

    private var logIdCounter = 1L
    private var templateIdCounter = 1L

    private fun updateLogsFlow() {
        logsFlow.value = logsMap.values.toList()
    }

    private fun updateTemplatesFlow() {
        templatesFlow.value = templatesMap.values.toList()
    }

    // Default Recurring Card Cost
    override fun getDefaultCardCost(): Flow<Double> = defaultCostFlow
    override suspend fun getDefaultCardCostDirect(): Double = defaultCostFlow.value
    override suspend fun setDefaultCardCost(cost: Double) {
        defaultCostFlow.value = cost
    }

    // Activity Logs
    override fun getAllLogs(): Flow<List<ActivityLog>> = logsFlow
    override suspend fun getAllLogsList(): List<ActivityLog> = logsMap.values.toList()
    override suspend fun getLogById(id: Long): ActivityLog? = logsMap[id]

    override fun getLogsByDateRange(startDate: String, endDate: String): Flow<List<ActivityLog>> {
        return logsFlow.map { list -> list.filter { it.date in startDate..endDate } }
    }

    override fun getLogsForMonth(yearMonth: String): Flow<List<ActivityLog>> {
        return logsFlow.map { list -> list.filter { it.date.startsWith(yearMonth) } }
    }

    override fun getSumCostForDateRange(startDate: String, endDate: String): Flow<Double> {
        return logsFlow.map { list -> list.filter { it.date in startDate..endDate }.sumOf { it.cost } }
    }

    override fun getSumCostForMonth(yearMonth: String): Flow<Double> {
        return logsFlow.map { list -> list.filter { it.date.startsWith(yearMonth) }.sumOf { it.cost } }
    }

    override fun getTotalSumCost(): Flow<Double> {
        return logsFlow.map { list -> list.sumOf { it.cost } }
    }

    override suspend fun insertLog(log: ActivityLog): Long {
        val id = if (log.id == 0L) logIdCounter++ else log.id
        val newLog = log.copy(id = id)
        logsMap[id] = newLog
        updateLogsFlow()
        return id
    }

    override suspend fun insertAllLogs(logs: List<ActivityLog>) {
        logs.forEach { insertLog(it) }
    }

    override suspend fun updateLog(log: ActivityLog) {
        logsMap[log.id] = log
        updateLogsFlow()
    }

    override suspend fun deleteLog(log: ActivityLog) {
        logsMap.remove(log.id)
        updateLogsFlow()
    }

    override suspend fun deleteLogById(id: Long) {
        logsMap.remove(id)
        updateLogsFlow()
    }

    override suspend fun deleteAllLogs() {
        logsMap.clear()
        updateLogsFlow()
    }

    // Templates
    override fun getAllTemplates(): Flow<List<ActivityTemplate>> = templatesFlow
    override suspend fun getAllTemplatesList(): List<ActivityTemplate> = templatesMap.values.toList()
    override suspend fun getTemplateById(id: Long): ActivityTemplate? = templatesMap[id]

    override suspend fun insertTemplate(template: ActivityTemplate): Long {
        val id = if (template.id == 0L) templateIdCounter++ else template.id
        val newT = template.copy(id = id)
        templatesMap[id] = newT
        updateTemplatesFlow()
        return id
    }

    override suspend fun insertAllTemplates(templates: List<ActivityTemplate>) {
        templates.forEach { insertTemplate(it) }
    }

    override suspend fun updateTemplate(template: ActivityTemplate) {
        templatesMap[template.id] = template
        updateTemplatesFlow()
    }

    override suspend fun deleteTemplate(template: ActivityTemplate) {
        templatesMap.remove(template.id)
        updateTemplatesFlow()
    }

    override suspend fun deleteTemplateById(id: Long) {
        templatesMap.remove(id)
        updateTemplatesFlow()
    }

    override suspend fun deleteAllTemplates() {
        templatesMap.clear()
        updateTemplatesFlow()
    }

    // Memberships
    override fun getAllMemberships(): Flow<List<MonthlyMembership>> = membershipsFlow
    override suspend fun getAllMembershipsList(): List<MonthlyMembership> = membershipsMap.values.toList()

    override fun getMembershipForMonth(yearMonth: String): Flow<MonthlyMembership> {
        return combine(membershipsFlow, defaultCostFlow) { list: List<MonthlyMembership>, defaultCost: Double ->
            list.find { it.yearMonth == yearMonth } ?: MonthlyMembership(yearMonth = yearMonth, cardCost = defaultCost)
        }
    }

    override suspend fun getMembershipForMonthDirect(yearMonth: String): MonthlyMembership {
        return membershipsMap[yearMonth] ?: MonthlyMembership(yearMonth = yearMonth, cardCost = defaultCostFlow.value)
    }

    override suspend fun ensureCurrentMonthMembership(yearMonth: String): MonthlyMembership {
        val existing = membershipsMap[yearMonth]
        if (existing != null) {
            return existing
        }
        val defaultCost = defaultCostFlow.value
        val newMembership = MonthlyMembership(yearMonth = yearMonth, cardCost = defaultCost)
        saveMembership(newMembership)
        return newMembership
    }

    override suspend fun saveMembership(membership: MonthlyMembership) {
        membershipsMap[membership.yearMonth] = membership
        membershipsFlow.value = membershipsMap.values.toList()
    }

    override suspend fun insertAllMemberships(memberships: List<MonthlyMembership>) {
        memberships.forEach { saveMembership(it) }
    }

    override suspend fun deleteMembership(membership: MonthlyMembership) {
        membershipsMap.remove(membership.yearMonth)
        membershipsFlow.value = membershipsMap.values.toList()
    }

    override suspend fun deleteMembershipByYearMonth(yearMonth: String) {
        membershipsMap.remove(yearMonth)
        membershipsFlow.value = membershipsMap.values.toList()
    }

    override suspend fun deleteAllMemberships() {
        membershipsMap.clear()
        membershipsFlow.value = emptyList()
    }
}
