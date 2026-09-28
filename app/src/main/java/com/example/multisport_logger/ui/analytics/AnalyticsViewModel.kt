package com.example.multisport_logger.ui.analytics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.multisport_logger.data.db.AppDatabase
import com.example.multisport_logger.data.entity.ActivityLog
import com.example.multisport_logger.data.entity.MonthlyMembership
import com.example.multisport_logger.data.repository.MultisportRepository
import com.example.multisport_logger.data.repository.MultisportRepositoryImpl
import com.example.multisport_logger.ui.activitylog.DateFormatterUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class VenueVisitCount(
    val venueName: String,
    val visitCount: Int,
    val totalValue: Double
)

data class AnalyticsUiState(
    val selectedMonth: String = DateFormatterUtil.currentMonthKey(),
    val availableMonths: List<String> = listOf(DateFormatterUtil.currentMonthKey()),
    val defaultCardCost: Double = 500.0,

    // Current Month Overview
    val monthVisitCount: Int = 0,
    val monthActivityValue: Double = 0.0,
    val monthCardCost: Double = 0.0,
    val monthNetSavings: Double = 0.0,
    val isMonthPositiveSavings: Boolean = true,
    val monthCostRecoveryRatio: Float = 0f,
    val topVenuesForMonth: List<VenueVisitCount> = emptyList(),

    // All-Time Summary
    val allTimeVisitCount: Int = 0,
    val allTimeActivityValue: Double = 0.0,
    val allTimeSubscriptionCost: Double = 0.0,
    val allTimeNetSavings: Double = 0.0,
    val isAllTimePositiveSavings: Boolean = true
)

class AnalyticsViewModel(
    repository: MultisportRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(DateFormatterUtil.currentMonthKey())

    val uiState: StateFlow<AnalyticsUiState> = combine(
        repository.getAllLogs(),
        repository.getAllMemberships(),
        repository.getDefaultCardCost(),
        _selectedMonth
    ) { logs: List<ActivityLog>, memberships: List<MonthlyMembership>, defaultCardCost: Double, selectedMonth: String ->

        // Extract all available months from logs and memberships + current month
        val logMonths = logs.map { DateFormatterUtil.extractMonthKey(it.date) }
        val membershipMonths = memberships.map { it.yearMonth }
        val currentMonth = DateFormatterUtil.currentMonthKey()

        val allMonthsSet = (logMonths + membershipMonths + currentMonth + selectedMonth).filter { it.length == 7 }.toSet()
        val availableMonthsSorted = allMonthsSet.sortedDescending()

        // Current Month Metrics
        val monthLogs = logs.filter { it.date.startsWith(selectedMonth) }
        val monthVisitCount = monthLogs.size
        val monthActivityValue = monthLogs.sumOf { it.cost }
        val monthMembership = memberships.find { it.yearMonth == selectedMonth }
        val monthCardCost = monthMembership?.cardCost ?: defaultCardCost
        val monthNetSavings = monthActivityValue - monthCardCost
        val isMonthPositive = monthNetSavings >= 0.0

        val recoveryRatio = if (monthCardCost > 0.0) {
            (monthActivityValue / monthCardCost).toFloat()
        } else {
            if (monthActivityValue > 0.0) 1.0f else 0.0f
        }

        val topVenues = monthLogs.groupBy { it.venueName }
            .map { (venue, list) ->
                VenueVisitCount(
                    venueName = venue,
                    visitCount = list.size,
                    totalValue = list.sumOf { it.cost }
                )
            }
            .sortedByDescending { it.visitCount }
            .take(4)

        // All-Time Summary Metrics
        val activeMonths = (logMonths + membershipMonths).filter { it.length == 7 }.toSet()
        val allTimeVisitCount = logs.size
        val allTimeActivityValue = logs.sumOf { it.cost }
        val allTimeSubscriptionCost = if (activeMonths.isEmpty()) {
            0.0
        } else {
            activeMonths.sumOf { ym ->
                memberships.find { it.yearMonth == ym }?.cardCost ?: defaultCardCost
            }
        }
        val allTimeNetSavings = allTimeActivityValue - allTimeSubscriptionCost
        val isAllTimePositive = allTimeNetSavings >= 0.0

        AnalyticsUiState(
            selectedMonth = selectedMonth,
            availableMonths = availableMonthsSorted,
            defaultCardCost = defaultCardCost,

            monthVisitCount = monthVisitCount,
            monthActivityValue = monthActivityValue,
            monthCardCost = monthCardCost,
            monthNetSavings = monthNetSavings,
            isMonthPositiveSavings = isMonthPositive,
            monthCostRecoveryRatio = recoveryRatio,
            topVenuesForMonth = topVenues,

            allTimeVisitCount = allTimeVisitCount,
            allTimeActivityValue = allTimeActivityValue,
            allTimeSubscriptionCost = allTimeSubscriptionCost,
            allTimeNetSavings = allTimeNetSavings,
            isAllTimePositiveSavings = isAllTimePositive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState()
    )

    fun setSelectedMonth(yearMonth: String) {
        if (yearMonth.isNotBlank()) {
            _selectedMonth.value = yearMonth
        }
    }

    fun selectPreviousMonth() {
        _selectedMonth.value = DateFormatterUtil.previousMonthKey(_selectedMonth.value)
    }

    fun selectNextMonth() {
        _selectedMonth.value = DateFormatterUtil.nextMonthKey(_selectedMonth.value)
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.getDatabase(context.applicationContext)
                val prefs = context.getSharedPreferences("multisport_prefs", Context.MODE_PRIVATE)
                val repository = MultisportRepositoryImpl(
                    activityDao = db.activityDao(),
                    templateDao = db.templateDao(),
                    membershipDao = db.membershipDao(),
                    sharedPreferences = prefs
                )
                return AnalyticsViewModel(repository) as T
            }
        }
    }
}
