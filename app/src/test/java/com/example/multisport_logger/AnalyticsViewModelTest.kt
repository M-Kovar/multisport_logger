package com.example.multisport_logger

import com.example.multisport_logger.data.entity.ActivityLog
import com.example.multisport_logger.data.entity.MonthlyMembership
import com.example.multisport_logger.ui.analytics.AnalyticsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeMultisportRepository
    private lateinit var viewModel: AnalyticsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeMultisportRepository(initialDefaultCost = 500.0)
        viewModel = AnalyticsViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialUiState_calculatesCurrentMonthAndAllTimeMetricsCorrectly() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // Logs for 2025-05
        fakeRepository.insertLog(ActivityLog(1, "2025-05-10", "Gym Central", "Workout", 200.0))
        fakeRepository.insertLog(ActivityLog(2, "2025-05-15", "Pool Side", "Swimming", 150.0))
        // Log for 2025-04
        fakeRepository.insertLog(ActivityLog(3, "2025-04-20", "Climbing Hub", "Bouldering", 250.0))

        // Subscriptions overrides
        fakeRepository.saveMembership(MonthlyMembership("2025-05", 500.0))
        fakeRepository.saveMembership(MonthlyMembership("2025-04", 400.0))

        viewModel.setSelectedMonth("2025-05")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value

        // Month metrics for 2025-05
        assertEquals("2025-05", state.selectedMonth)
        assertEquals(2, state.monthVisitCount)
        assertEquals(350.0, state.monthActivityValue, 0.01)
        assertEquals(500.0, state.monthCardCost, 0.01)
        assertEquals(-150.0, state.monthNetSavings, 0.01) // 350 - 500 = -150
        assertFalse(state.isMonthPositiveSavings)

        // All-time metrics
        assertEquals(3, state.allTimeVisitCount)
        assertEquals(600.0, state.allTimeActivityValue, 0.01)
        assertEquals(900.0, state.allTimeSubscriptionCost, 0.01)
        assertEquals(-300.0, state.allTimeNetSavings, 0.01) // 600 - 900 = -300

        collectJob.cancel()
    }

    @Test
    fun positiveSavings_calculatedCorrectly() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        fakeRepository.insertLog(ActivityLog(1, "2025-05-01", "Gym Alpha", "Gym Session", 300.0))
        fakeRepository.insertLog(ActivityLog(2, "2025-05-05", "Gym Alpha", "Gym Session", 300.0))
        fakeRepository.insertLog(ActivityLog(3, "2025-05-10", "Pool Beta", "Swimming", 200.0))

        fakeRepository.saveMembership(MonthlyMembership("2025-05", 500.0))

        viewModel.setSelectedMonth("2025-05")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.monthVisitCount)
        assertEquals(800.0, state.monthActivityValue, 0.01)
        assertEquals(500.0, state.monthCardCost, 0.01)
        assertEquals(300.0, state.monthNetSavings, 0.01) // 800 - 500 = 300
        assertTrue(state.isMonthPositiveSavings)

        collectJob.cancel()
    }

    @Test
    fun monthWithoutOverride_autoPopulatesDefaultCardCost() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // Set default card cost to 500
        fakeRepository.setDefaultCardCost(500.0)

        // Month 2025-06 has logs but NO custom monthly membership override
        fakeRepository.insertLog(ActivityLog(1, "2025-06-05", "Gym Gamma", "Workout", 650.0))

        viewModel.setSelectedMonth("2025-06")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("2025-06", state.selectedMonth)
        // Automatically populated default card cost:
        assertEquals(500.0, state.monthCardCost, 0.01)
        assertEquals(650.0, state.monthActivityValue, 0.01)
        assertEquals(150.0, state.monthNetSavings, 0.01) // 650 - 500 = 150
        assertTrue(state.isMonthPositiveSavings)

        collectJob.cancel()
    }

    @Test
    fun monthWithOverride_usesCustomCardCostInsteadOfDefault() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // Default card cost is 500.0, but 2025-07 has an individual override of 750.0
        fakeRepository.setDefaultCardCost(500.0)
        fakeRepository.saveMembership(MonthlyMembership("2025-07", 750.0))
        fakeRepository.insertLog(ActivityLog(1, "2025-07-02", "Tennis Club", "Tennis", 900.0))

        viewModel.setSelectedMonth("2025-07")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(750.0, state.monthCardCost, 0.01)
        assertEquals(900.0, state.monthActivityValue, 0.01)
        assertEquals(150.0, state.monthNetSavings, 0.01)

        collectJob.cancel()
    }

    @Test
    fun recurringMonthlySubscription_calculatesAllTimeCostsWithDefaultsAndOverrides() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        fakeRepository.setDefaultCardCost(500.0)

        // Month 1: 2025-04 has custom override of 400
        fakeRepository.saveMembership(MonthlyMembership("2025-04", 400.0))
        fakeRepository.insertLog(ActivityLog(1, "2025-04-10", "Gym", "Workout", 300.0))

        // Month 2: 2025-05 has NO override -> auto-populates default 500
        fakeRepository.insertLog(ActivityLog(2, "2025-05-10", "Pool", "Swim", 350.0))

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        // Total activity value = 300 + 350 = 650
        assertEquals(650.0, state.allTimeActivityValue, 0.01)
        // Total subscription = 400 (custom) + 500 (auto-populated default) = 900
        assertEquals(900.0, state.allTimeSubscriptionCost, 0.01)
        assertEquals(-250.0, state.allTimeNetSavings, 0.01)

        collectJob.cancel()
    }

    @Test
    fun selectPreviousMonth_and_selectNextMonth_navigatesMonths() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.setSelectedMonth("2025-05")
        testScheduler.advanceUntilIdle()

        viewModel.selectPreviousMonth()
        testScheduler.advanceUntilIdle()
        assertEquals("2025-04", viewModel.uiState.value.selectedMonth)

        viewModel.selectNextMonth()
        testScheduler.advanceUntilIdle()
        assertEquals("2025-05", viewModel.uiState.value.selectedMonth)

        collectJob.cancel()
    }
}
