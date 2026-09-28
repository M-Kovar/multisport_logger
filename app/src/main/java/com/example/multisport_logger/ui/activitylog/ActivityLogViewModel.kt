package com.example.multisport_logger.ui.activitylog

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.multisport_logger.data.db.AppDatabase
import com.example.multisport_logger.data.entity.ActivityLog
import com.example.multisport_logger.data.entity.ActivityTemplate
import com.example.multisport_logger.data.repository.MultisportRepository
import com.example.multisport_logger.data.repository.MultisportRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

enum class SortOrder(val label: String) {
    DATE_DESC("Nejnovější"),
    DATE_ASC("Nejstarší"),
    COST_DESC("Nejvyšší cena"),
    COST_ASC("Nejnižší cena"),
    VENUE_ASC("Název místa")
}

enum class GroupBy(val label: String) {
    MONTH("Měsíc"),
    VENUE("Místo"),
    NONE("Žádné")
}

data class ActivityLogUiState(
    val allLogs: List<ActivityLog> = emptyList(),
    val templates: List<ActivityTemplate> = emptyList(),
    val groupedLogs: Map<String, List<ActivityLog>> = emptyMap(),
    val totalFilteredCost: Double = 0.0,
    val totalFilteredCount: Int = 0,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DATE_DESC,
    val groupBy: GroupBy = GroupBy.MONTH,
    val selectedMonthFilter: String? = null,
    val availableMonths: List<String> = emptyList(),

    // Add/Edit Activity Log Form State
    val isAddEditLogDialogOpen: Boolean = false,
    val editingLogId: Long? = null,
    val logDate: String = DateFormatterUtil.todayIso(),
    val logVenueName: String = "",
    val logActivityName: String = "",
    val logCost: String = "",
    val saveAsTemplate: Boolean = false,
    val logFormError: String? = null,

    // Templates Management State
    val isTemplatesManagerOpen: Boolean = false,

    // Add/Edit Template Form State
    val isAddEditTemplateDialogOpen: Boolean = false,
    val editingTemplateId: Long? = null,
    val templateVenueName: String = "",
    val templateActivityName: String = "",
    val templateDefaultCost: String = "",
    val templateFormError: String? = null,

    // Delete confirmation state
    val logToDelete: ActivityLog? = null,
    val templateToDelete: ActivityTemplate? = null,

    // User feedback / snackbar
    val userMessage: String? = null
)

class ActivityLogViewModel(
    private val repository: MultisportRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    private val _groupBy = MutableStateFlow(GroupBy.MONTH)
    private val _selectedMonthFilter = MutableStateFlow<String?>(null)

    private data class FilterState(
        val query: String = "",
        val sort: SortOrder = SortOrder.DATE_DESC,
        val group: GroupBy = GroupBy.MONTH,
        val monthFilter: String? = null
    )

    private val _filterState = combine(
        _searchQuery,
        _sortOrder,
        _groupBy,
        _selectedMonthFilter
    ) { query, sort, group, monthFilter ->
        FilterState(query, sort, group, monthFilter)
    }

    private data class DialogState(
        val isAddEditLogDialogOpen: Boolean = false,
        val editingLogId: Long? = null,
        val logDate: String = DateFormatterUtil.todayIso(),
        val logVenueName: String = "",
        val logActivityName: String = "",
        val logCost: String = "",
        val saveAsTemplate: Boolean = false,
        val logFormError: String? = null,

        val isTemplatesManagerOpen: Boolean = false,

        val isAddEditTemplateDialogOpen: Boolean = false,
        val editingTemplateId: Long? = null,
        val templateVenueName: String = "",
        val templateActivityName: String = "",
        val templateDefaultCost: String = "",
        val templateFormError: String? = null,

        val logToDelete: ActivityLog? = null,
        val templateToDelete: ActivityTemplate? = null,

        val userMessage: String? = null
    )

    private val _dialogState = MutableStateFlow(DialogState())

    val uiState: StateFlow<ActivityLogUiState> = combine(
        repository.getAllLogs(),
        repository.getAllTemplates(),
        _filterState,
        _dialogState
    ) { logs: List<ActivityLog>, templates: List<ActivityTemplate>, filter: FilterState, dialogs: DialogState ->

        // Calculate available months from all logs (sorted descending)
        val months = logs.map { DateFormatterUtil.extractMonthKey(it.date) }
            .distinct()
            .sortedDescending()

        // Filter logs
        var filteredLogs = logs

        if (!filter.monthFilter.isNullOrBlank()) {
            filteredLogs = filteredLogs.filter { it.date.startsWith(filter.monthFilter) }
        }

        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            filteredLogs = filteredLogs.filter {
                it.venueName.lowercase().contains(q) ||
                        it.activityName.lowercase().contains(q) ||
                        DateFormatterUtil.formatToDisplay(it.date).lowercase().contains(q)
            }
        }

        // Sort logs
        filteredLogs = when (filter.sort) {
            SortOrder.DATE_DESC -> filteredLogs.sortedWith(compareByDescending<ActivityLog> { it.date }.thenByDescending { it.id })
            SortOrder.DATE_ASC -> filteredLogs.sortedWith(compareBy<ActivityLog> { it.date }.thenBy { it.id })
            SortOrder.COST_DESC -> filteredLogs.sortedWith(compareByDescending<ActivityLog> { it.cost }.thenByDescending { it.date })
            SortOrder.COST_ASC -> filteredLogs.sortedWith(compareBy<ActivityLog> { it.cost }.thenByDescending { it.date })
            SortOrder.VENUE_ASC -> filteredLogs.sortedWith(compareBy<ActivityLog> { it.venueName.lowercase() }.thenByDescending { it.date })
        }

        // Group logs
        val groupedMap: Map<String, List<ActivityLog>> = when (filter.group) {
            GroupBy.MONTH -> {
                filteredLogs.groupBy { DateFormatterUtil.formatToMonthYear(it.date) }
            }
            GroupBy.VENUE -> {
                filteredLogs.groupBy { it.venueName }
            }
            GroupBy.NONE -> {
                if (filteredLogs.isNotEmpty()) mapOf("All Activities" to filteredLogs) else emptyMap()
            }
        }

        val totalCost = filteredLogs.sumOf { it.cost }

        ActivityLogUiState(
            allLogs = logs,
            templates = templates,
            groupedLogs = groupedMap,
            totalFilteredCost = totalCost,
            totalFilteredCount = filteredLogs.size,
            searchQuery = filter.query,
            sortOrder = filter.sort,
            groupBy = filter.group,
            selectedMonthFilter = filter.monthFilter,
            availableMonths = months,

            isAddEditLogDialogOpen = dialogs.isAddEditLogDialogOpen,
            editingLogId = dialogs.editingLogId,
            logDate = dialogs.logDate,
            logVenueName = dialogs.logVenueName,
            logActivityName = dialogs.logActivityName,
            logCost = dialogs.logCost,
            saveAsTemplate = dialogs.saveAsTemplate,
            logFormError = dialogs.logFormError,

            isTemplatesManagerOpen = dialogs.isTemplatesManagerOpen,

            isAddEditTemplateDialogOpen = dialogs.isAddEditTemplateDialogOpen,
            editingTemplateId = dialogs.editingTemplateId,
            templateVenueName = dialogs.templateVenueName,
            templateActivityName = dialogs.templateActivityName,
            templateDefaultCost = dialogs.templateDefaultCost,
            templateFormError = dialogs.templateFormError,

            logToDelete = dialogs.logToDelete,
            templateToDelete = dialogs.templateToDelete,
            userMessage = dialogs.userMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ActivityLogUiState()
    )

    // Filter/Sort setters
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun setGroupBy(groupBy: GroupBy) {
        _groupBy.value = groupBy
    }

    fun setSelectedMonthFilter(month: String?) {
        _selectedMonthFilter.value = month
    }

    fun userMessageShown() {
        _dialogState.update { it.copy(userMessage = null) }
    }

    // --- Log Form Actions ---

    fun openAddLogDialog(fromTemplate: ActivityTemplate? = null) {
        _dialogState.update {
            it.copy(
                isAddEditLogDialogOpen = true,
                editingLogId = null,
                logDate = DateFormatterUtil.todayIso(),
                logVenueName = fromTemplate?.venueName ?: "",
                logActivityName = fromTemplate?.activityName ?: "",
                logCost = fromTemplate?.defaultCost?.let { c -> String.format(Locale.US, "%.2f", c) } ?: "",
                saveAsTemplate = false,
                logFormError = null
            )
        }
    }

    fun openEditLogDialog(log: ActivityLog) {
        _dialogState.update {
            it.copy(
                isAddEditLogDialogOpen = true,
                editingLogId = log.id,
                logDate = log.date,
                logVenueName = log.venueName,
                logActivityName = log.activityName,
                logCost = String.format(Locale.US, "%.2f", log.cost),
                saveAsTemplate = false,
                logFormError = null
            )
        }
    }

    fun closeAddEditLogDialog() {
        _dialogState.update { it.copy(isAddEditLogDialogOpen = false, logFormError = null) }
    }

    fun updateLogDateField(date: String) {
        _dialogState.update { it.copy(logDate = date, logFormError = null) }
    }

    fun updateLogVenueField(venue: String) {
        _dialogState.update { it.copy(logVenueName = venue, logFormError = null) }
    }

    fun updateLogActivityField(activity: String) {
        _dialogState.update { it.copy(logActivityName = activity, logFormError = null) }
    }

    fun updateLogCostField(cost: String) {
        _dialogState.update { it.copy(logCost = cost, logFormError = null) }
    }

    fun updateSaveAsTemplate(save: Boolean) {
        _dialogState.update { it.copy(saveAsTemplate = save) }
    }

    fun saveActivityLog() {
        val state = _dialogState.value
        val venue = state.logVenueName.trim()
        val activity = state.logActivityName.trim()
        val costText = state.logCost.trim()
        val date = state.logDate.trim()

        if (venue.isBlank()) {
            _dialogState.update { it.copy(logFormError = "Zadejte název místa.") }
            return
        }
        if (activity.isBlank()) {
            _dialogState.update { it.copy(logFormError = "Zadejte název aktivity.") }
            return
        }
        val costDouble = costText.toDoubleOrNull()
        if (costDouble == null || costDouble < 0) {
            _dialogState.update { it.copy(logFormError = "Zadejte platnou cenu (např. 150).") }
            return
        }
        if (!DateFormatterUtil.isValidIsoDate(date)) {
            _dialogState.update { it.copy(logFormError = "Neplatný formát data. Použijte RRRR-MM-DD.") }
            return
        }

        viewModelScope.launch {
            val logId = state.editingLogId
            if (logId == null) {
                val newLog = ActivityLog(
                    date = date,
                    venueName = venue,
                    activityName = activity,
                    cost = costDouble
                )
                repository.insertLog(newLog)
                _dialogState.update { it.copy(userMessage = "Aktivita byla úspěšně zaznamenána!") }
            } else {
                val updatedLog = ActivityLog(
                    id = logId,
                    date = date,
                    venueName = venue,
                    activityName = activity,
                    cost = costDouble
                )
                repository.updateLog(updatedLog)
                _dialogState.update { it.copy(userMessage = "Záznam aktivity byl upraven!") }
            }

            if (state.saveAsTemplate) {
                repository.insertTemplate(
                    ActivityTemplate(
                        venueName = venue,
                        activityName = activity,
                        defaultCost = costDouble
                    )
                )
            }

            _dialogState.update {
                it.copy(
                    isAddEditLogDialogOpen = false,
                    logFormError = null
                )
            }
        }
    }

    // --- Quick Log From Template ---
    fun quickLogFromTemplate(template: ActivityTemplate) {
        viewModelScope.launch {
            val today = DateFormatterUtil.todayIso()
            val newLog = ActivityLog(
                date = today,
                venueName = template.venueName,
                activityName = template.activityName,
                cost = template.defaultCost
            )
            repository.insertLog(newLog)
            _dialogState.update {
                it.copy(userMessage = "Zaznamenáno: ${template.activityName} v ${template.venueName} na dnešek!")
            }
        }
    }

    // --- Delete Log ---
    fun promptDeleteLog(log: ActivityLog) {
        _dialogState.update { it.copy(logToDelete = log) }
    }

    fun dismissDeleteLog() {
        _dialogState.update { it.copy(logToDelete = null) }
    }

    fun confirmDeleteLog() {
        val log = _dialogState.value.logToDelete ?: return
        viewModelScope.launch {
            repository.deleteLog(log)
            _dialogState.update {
                it.copy(logToDelete = null, userMessage = "Záznam aktivity byl smazán")
            }
        }
    }

    // --- Template Management ---
    fun openTemplatesManager() {
        _dialogState.update { it.copy(isTemplatesManagerOpen = true) }
    }

    fun closeTemplatesManager() {
        _dialogState.update { it.copy(isTemplatesManagerOpen = false) }
    }

    fun openAddTemplateDialog() {
        _dialogState.update {
            it.copy(
                isAddEditTemplateDialogOpen = true,
                editingTemplateId = null,
                templateVenueName = "",
                templateActivityName = "",
                templateDefaultCost = "",
                templateFormError = null
            )
        }
    }

    fun openEditTemplateDialog(template: ActivityTemplate) {
        val formattedCost = if (template.defaultCost % 1.0 == 0.0) {
            template.defaultCost.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", template.defaultCost)
        }
        _dialogState.update {
            it.copy(
                isAddEditTemplateDialogOpen = true,
                editingTemplateId = template.id,
                templateVenueName = template.venueName,
                templateActivityName = template.activityName,
                templateDefaultCost = formattedCost,
                templateFormError = null
            )
        }
    }

    fun closeAddEditTemplateDialog() {
        _dialogState.update { it.copy(isAddEditTemplateDialogOpen = false, templateFormError = null) }
    }

    fun updateTemplateVenueField(venue: String) {
        _dialogState.update { it.copy(templateVenueName = venue, templateFormError = null) }
    }

    fun updateTemplateActivityField(activity: String) {
        _dialogState.update { it.copy(templateActivityName = activity, templateFormError = null) }
    }

    fun updateTemplateCostField(cost: String) {
        _dialogState.update { it.copy(templateDefaultCost = cost, templateFormError = null) }
    }

    fun saveTemplate() {
        val state = _dialogState.value
        val venue = state.templateVenueName.trim()
        val activity = state.templateActivityName.trim()
        val costText = state.templateDefaultCost.trim()

        if (venue.isBlank()) {
            _dialogState.update { it.copy(templateFormError = "Zadejte název místa.") }
            return
        }
        if (activity.isBlank()) {
            _dialogState.update { it.copy(templateFormError = "Zadejte název aktivity.") }
            return
        }
        val costDouble = costText.toDoubleOrNull()
        if (costDouble == null || costDouble < 0) {
            _dialogState.update { it.copy(templateFormError = "Zadejte platnou cenu (např. 150).") }
            return
        }

        viewModelScope.launch {
            val templateId = state.editingTemplateId
            if (templateId == null) {
                repository.insertTemplate(
                    ActivityTemplate(
                        venueName = venue,
                        activityName = activity,
                        defaultCost = costDouble
                    )
                )
                _dialogState.update { it.copy(userMessage = "Šablona byla vytvořena!") }
            } else {
                repository.updateTemplate(
                    ActivityTemplate(
                        id = templateId,
                        venueName = venue,
                        activityName = activity,
                        defaultCost = costDouble
                    )
                )
                _dialogState.update { it.copy(userMessage = "Šablona byla upravena!") }
            }
            _dialogState.update {
                it.copy(
                    isAddEditTemplateDialogOpen = false,
                    templateFormError = null
                )
            }
        }
    }

    fun promptDeleteTemplate(template: ActivityTemplate) {
        _dialogState.update { it.copy(templateToDelete = template) }
    }

    fun dismissDeleteTemplate() {
        _dialogState.update { it.copy(templateToDelete = null) }
    }

    fun confirmDeleteTemplate() {
        val template = _dialogState.value.templateToDelete ?: return
        viewModelScope.launch {
            repository.deleteTemplate(template)
            _dialogState.update {
                it.copy(templateToDelete = null, userMessage = "Šablona byla smazána")
            }
        }
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
                return ActivityLogViewModel(repository) as T
            }
        }
    }
}
