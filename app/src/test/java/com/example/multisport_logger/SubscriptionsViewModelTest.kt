package com.example.multisport_logger

import com.example.multisport_logger.data.entity.ActivityLog
import com.example.multisport_logger.data.entity.MonthlyMembership
import com.example.multisport_logger.ui.subscriptions.ExportFormat
import com.example.multisport_logger.ui.subscriptions.SubscriptionsViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeMultisportRepository
    private lateinit var viewModel: SubscriptionsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeMultisportRepository(initialDefaultCost = 500.0)
        viewModel = SubscriptionsViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun defaultCardCost_isLoadedAndCanBeAdjusted() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        testScheduler.advanceUntilIdle()
        assertEquals(500.0, viewModel.uiState.value.defaultCardCost, 0.01)

        viewModel.openEditDefaultCostDialog()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isEditDefaultCostDialogOpen)
        assertEquals("500", viewModel.uiState.value.inputDefaultCardCost)

        viewModel.updateInputDefaultCost("600")
        viewModel.saveDefaultCardCost()
        testScheduler.advanceUntilIdle()

        assertEquals(600.0, fakeRepository.getDefaultCardCostDirect(), 0.01)
        assertEquals(600.0, viewModel.uiState.value.defaultCardCost, 0.01)

        collectJob.cancel()
    }

    @Test
    fun openAddSubscriptionDialog_autoPopulatesDefaultCardCost() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        fakeRepository.setDefaultCardCost(550.0)
        testScheduler.advanceUntilIdle()

        viewModel.openAddSubscriptionDialog("2025-05")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isAddEditDialogOpen)
        assertEquals("2025-05", state.inputYearMonth)
        assertEquals("550", state.inputCardCost)

        collectJob.cancel()
    }

    @Test
    fun saveSubscription_savesCardCostToRepository() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.openAddSubscriptionDialog("2025-05")
        testScheduler.advanceUntilIdle()

        // User can override the auto-populated default cost
        viewModel.updateInputCardCost("650")
        viewModel.saveSubscription()
        testScheduler.advanceUntilIdle()

        val memberships = fakeRepository.getAllMembershipsList()
        assertEquals(1, memberships.size)
        assertEquals("2025-05", memberships[0].yearMonth)
        assertEquals(650.0, memberships[0].cardCost, 0.01)

        collectJob.cancel()
    }

    @Test
    fun deleteSubscription_removesMembershipFromRepository() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val membership = MonthlyMembership("2025-05", 500.0)
        fakeRepository.saveMembership(membership)
        testScheduler.advanceUntilIdle()

        viewModel.promptDeleteSubscription(membership)
        viewModel.confirmDeleteSubscription()
        testScheduler.advanceUntilIdle()

        val memberships = fakeRepository.getAllMembershipsList()
        assertTrue(memberships.isEmpty())

        collectJob.cancel()
    }

    @Test
    fun openExportDialog_generatesExportContent() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        fakeRepository.insertLog(ActivityLog(1, "2025-05-10", "Gym Alpha", "Workout", 150.0))
        fakeRepository.saveMembership(MonthlyMembership("2025-05", 500.0))

        viewModel.openExportDialog(ExportFormat.JSON)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isExportDialogOpen)
        assertTrue(state.exportedDataText.contains("Gym Alpha"))
        assertTrue(state.exportedDataText.contains("2025-05"))

        collectJob.cancel()
    }

    @Test
    fun executeImport_withReplaceMode_clearsOldAndImportsNew() = runTest {
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // Old log in repository
        fakeRepository.insertLog(ActivityLog(1, "2025-01-01", "Old Gym", "Old Activity", 100.0))

        val importJson = """
            {
                "activityLogs": [
                    {"id": 100, "date": "2025-05-20", "venueName": "New Gym", "activityName": "New Workout", "cost": 250.0}
                ],
                "templates": [
                    {"id": 200, "venueName": "New Gym", "activityName": "New Workout", "defaultCost": 250.0}
                ],
                "monthlyMemberships": [
                    {"yearMonth": "2025-05", "cardCost": 600.0}
                ]
            }
        """.trimIndent()

        viewModel.openImportDialog()
        viewModel.updateImportText(importJson)
        viewModel.updateImportReplaceMode(true)

        viewModel.executeImport()
        testScheduler.advanceUntilIdle()

        val logs = fakeRepository.getAllLogsList()
        assertEquals(1, logs.size)
        assertEquals("New Gym", logs[0].venueName)

        val templates = fakeRepository.getAllTemplatesList()
        assertEquals(1, templates.size)

        val memberships = fakeRepository.getAllMembershipsList()
        assertEquals(1, memberships.size)
        assertEquals(600.0, memberships[0].cardCost, 0.01)

        collectJob.cancel()
    }

    @Test
    fun ensureCurrentMonthMembership_insertsDefaultCost_whenNotPresent() = runTest {
        val currentMonth = "2025-06"
        fakeRepository.setDefaultCardCost(520.0)

        val result = fakeRepository.ensureCurrentMonthMembership(currentMonth)

        assertEquals(currentMonth, result.yearMonth)
        assertEquals(520.0, result.cardCost, 0.01)

        val stored = fakeRepository.getMembershipForMonthDirect(currentMonth)
        assertEquals(520.0, stored.cardCost, 0.01)
        assertEquals(1, fakeRepository.getAllMembershipsList().size)
    }

    @Test
    fun ensureCurrentMonthMembership_doesNotOverwrite_whenAlreadyPresent() = runTest {
        val currentMonth = "2025-06"
        fakeRepository.setDefaultCardCost(520.0)
        fakeRepository.saveMembership(MonthlyMembership(currentMonth, 750.0))

        val result = fakeRepository.ensureCurrentMonthMembership(currentMonth)

        assertEquals(currentMonth, result.yearMonth)
        assertEquals(750.0, result.cardCost, 0.01) // Preserves custom cost
        assertEquals(1, fakeRepository.getAllMembershipsList().size)
    }
}
