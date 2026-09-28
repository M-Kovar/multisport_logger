package com.example.multisport_logger

import com.example.multisport_logger.data.entity.ActivityLog
import com.example.multisport_logger.data.entity.ActivityTemplate
import com.example.multisport_logger.data.entity.MonthlyMembership
import com.example.multisport_logger.data.repository.MultisportRepository
import com.example.multisport_logger.ui.activitylog.ActivityLogViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityLogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeMultisportRepository
    private lateinit var viewModel: ActivityLogViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeMultisportRepository()
        viewModel = ActivityLogViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialUiState_reflectsRepositoryLogsAndTemplates() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val log1 = ActivityLog(1, "2025-05-10", "Gym Central", "Workout", 12.0)
        val log2 = ActivityLog(2, "2025-05-15", "Pool Side", "Swimming", 15.0)
        fakeRepository.insertLog(log1)
        fakeRepository.insertLog(log2)

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.allLogs.size)
        assertEquals(27.0, state.totalFilteredCost, 0.01)
        assertEquals(2, state.totalFilteredCount)

        collectJob.cancel()
    }

    @Test
    fun setSearchQuery_filtersLogsByVenueOrActivity() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        fakeRepository.insertLog(ActivityLog(1, "2025-05-10", "Gym Central", "Workout", 12.0))
        fakeRepository.insertLog(ActivityLog(2, "2025-05-15", "Climbing Hub", "Bouldering", 18.0))

        viewModel.setSearchQuery("climbing")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.totalFilteredCount)
        assertEquals(18.0, state.totalFilteredCost, 0.01)

        collectJob.cancel()
    }

    @Test
    fun openAddLogDialog_and_saveActivityLog_insertsToRepo() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.openAddLogDialog()
        viewModel.updateLogDateField("2025-05-20")
        viewModel.updateLogVenueField("Fit Studio")
        viewModel.updateLogActivityField("Pilates")
        viewModel.updateLogCostField("20.00")
        viewModel.updateSaveAsTemplate(true)

        viewModel.saveActivityLog()
        testScheduler.advanceUntilIdle()

        val logs = fakeRepository.getAllLogsList()
        assertEquals(1, logs.size)
        assertEquals("Fit Studio", logs[0].venueName)
        assertEquals("Pilates", logs[0].activityName)
        assertEquals(20.0, logs[0].cost, 0.01)

        val templates = fakeRepository.getAllTemplatesList()
        assertEquals(1, templates.size)
        assertEquals("Fit Studio", templates[0].venueName)

        collectJob.cancel()
    }

    @Test
    fun quickLogFromTemplate_createsLogWithTodayDate() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val template = ActivityTemplate(1, "Local Gym", "Weightlifting", 10.0)
        fakeRepository.insertTemplate(template)

        viewModel.quickLogFromTemplate(template)
        testScheduler.advanceUntilIdle()

        val logs = fakeRepository.getAllLogsList()
        assertEquals(1, logs.size)
        assertEquals("Local Gym", logs[0].venueName)
        assertEquals(10.0, logs[0].cost, 0.01)

        collectJob.cancel()
    }

    @Test
    fun deleteLog_removesItemFromRepo() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val log = ActivityLog(1, "2025-05-10", "Gym Central", "Workout", 12.0)
        fakeRepository.insertLog(log)
        testScheduler.advanceUntilIdle()

        viewModel.promptDeleteLog(log)
        viewModel.confirmDeleteLog()
        testScheduler.advanceUntilIdle()

        val logs = fakeRepository.getAllLogsList()
        assertTrue(logs.isEmpty())

        collectJob.cancel()
    }
}
