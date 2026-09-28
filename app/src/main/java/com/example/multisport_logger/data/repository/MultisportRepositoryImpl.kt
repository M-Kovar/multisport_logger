package com.example.multisport_logger.data.repository

import android.content.SharedPreferences
import com.example.multisport_logger.data.dao.ActivityDao
import com.example.multisport_logger.data.dao.MembershipDao
import com.example.multisport_logger.data.dao.TemplateDao
import com.example.multisport_logger.data.entity.ActivityLog
import com.example.multisport_logger.data.entity.ActivityTemplate
import com.example.multisport_logger.data.entity.MonthlyMembership
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class MultisportRepositoryImpl(
    private val activityDao: ActivityDao,
    private val templateDao: TemplateDao,
    private val membershipDao: MembershipDao,
    private val sharedPreferences: SharedPreferences? = null,
    initialDefaultCardCost: Double = 500.0
) : MultisportRepository {

    private val defaultCostKey = "default_card_cost"

    private val _defaultCardCost = MutableStateFlow(
        sharedPreferences?.getFloat(defaultCostKey, initialDefaultCardCost.toFloat())?.toDouble()
            ?: initialDefaultCardCost
    )

    override fun getDefaultCardCost(): Flow<Double> = _defaultCardCost

    override suspend fun getDefaultCardCostDirect(): Double = _defaultCardCost.value

    override suspend fun setDefaultCardCost(cost: Double) {
        sharedPreferences?.edit()?.putFloat(defaultCostKey, cost.toFloat())?.apply()
        _defaultCardCost.value = cost
    }

    override fun getAllLogs(): Flow<List<ActivityLog>> = activityDao.getAllLogs()

    override suspend fun getAllLogsList(): List<ActivityLog> = activityDao.getAllLogsList()

    override suspend fun getLogById(id: Long): ActivityLog? = activityDao.getById(id)

    override fun getLogsByDateRange(startDate: String, endDate: String): Flow<List<ActivityLog>> =
        activityDao.getLogsByDateRange(startDate, endDate)

    override fun getLogsForMonth(yearMonth: String): Flow<List<ActivityLog>> =
        activityDao.getLogsForMonth(yearMonth)

    override fun getSumCostForDateRange(startDate: String, endDate: String): Flow<Double> =
        activityDao.getSumCostForDateRange(startDate, endDate)

    override fun getSumCostForMonth(yearMonth: String): Flow<Double> =
        activityDao.getSumCostForMonth(yearMonth)

    override fun getTotalSumCost(): Flow<Double> = activityDao.getTotalSumCost()

    override suspend fun insertLog(log: ActivityLog): Long = activityDao.insert(log)

    override suspend fun insertAllLogs(logs: List<ActivityLog>) = activityDao.insertAll(logs)

    override suspend fun updateLog(log: ActivityLog) = activityDao.update(log)

    override suspend fun deleteLog(log: ActivityLog) = activityDao.delete(log)

    override suspend fun deleteLogById(id: Long) = activityDao.deleteById(id)

    override suspend fun deleteAllLogs() = activityDao.deleteAll()

    // Activity Templates
    override fun getAllTemplates(): Flow<List<ActivityTemplate>> = templateDao.getAllTemplates()

    override suspend fun getAllTemplatesList(): List<ActivityTemplate> = templateDao.getAllTemplatesList()

    override suspend fun getTemplateById(id: Long): ActivityTemplate? = templateDao.getById(id)

    override suspend fun insertTemplate(template: ActivityTemplate): Long = templateDao.insert(template)

    override suspend fun insertAllTemplates(templates: List<ActivityTemplate>) = templateDao.insertAll(templates)

    override suspend fun updateTemplate(template: ActivityTemplate) = templateDao.update(template)

    override suspend fun deleteTemplate(template: ActivityTemplate) = templateDao.delete(template)

    override suspend fun deleteTemplateById(id: Long) = templateDao.deleteById(id)

    override suspend fun deleteAllTemplates() = templateDao.deleteAll()

    // Monthly Memberships
    override fun getAllMemberships(): Flow<List<MonthlyMembership>> = membershipDao.getAllMemberships()

    override suspend fun getAllMembershipsList(): List<MonthlyMembership> = membershipDao.getAllMembershipsList()

    override fun getMembershipForMonth(yearMonth: String): Flow<MonthlyMembership> =
        combine(membershipDao.getMembershipForMonth(yearMonth), getDefaultCardCost()) { membership, defaultCost ->
            membership ?: MonthlyMembership(yearMonth = yearMonth, cardCost = defaultCost)
        }

    override suspend fun getMembershipForMonthDirect(yearMonth: String): MonthlyMembership {
        val existing = membershipDao.getMembershipForMonthDirect(yearMonth)
        return existing ?: MonthlyMembership(yearMonth = yearMonth, cardCost = getDefaultCardCostDirect())
    }

    override suspend fun ensureCurrentMonthMembership(yearMonth: String): MonthlyMembership {
        val existing = membershipDao.getMembershipForMonthDirect(yearMonth)
        if (existing != null) {
            return existing
        }
        val defaultCost = getDefaultCardCostDirect()
        val newMembership = MonthlyMembership(yearMonth = yearMonth, cardCost = defaultCost)
        membershipDao.insertOrUpdate(newMembership)
        return newMembership
    }

    override suspend fun saveMembership(membership: MonthlyMembership) =
        membershipDao.insertOrUpdate(membership)

    override suspend fun insertAllMemberships(memberships: List<MonthlyMembership>) =
        membershipDao.insertAll(memberships)

    override suspend fun deleteMembership(membership: MonthlyMembership) =
        membershipDao.delete(membership)

    override suspend fun deleteMembershipByYearMonth(yearMonth: String) =
        membershipDao.deleteByYearMonth(yearMonth)

    override suspend fun deleteAllMemberships() = membershipDao.deleteAll()
}
