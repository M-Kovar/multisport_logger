package com.example.multisport_logger.ui.subscriptions

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.multisport_logger.data.db.AppDatabase
import com.example.multisport_logger.data.entity.MonthlyMembership
import com.example.multisport_logger.data.repository.MultisportRepository
import com.example.multisport_logger.data.repository.MultisportRepositoryImpl
import com.example.multisport_logger.data.util.CurrencyUtil
import com.example.multisport_logger.data.util.DataExportImportUtil
import com.example.multisport_logger.ui.activitylog.DateFormatterUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

enum class ExportFormat {
    JSON,
    CSV
}

data class SubscriptionsUiState(
    val defaultCardCost: Double = 500.0,
    val memberships: List<MonthlyMembership> = emptyList(),

    // Edit Default Cost Dialog
    val isEditDefaultCostDialogOpen: Boolean = false,
    val inputDefaultCardCost: String = "",
    val defaultCostError: String? = null,

    // Add/Edit Monthly Subscription Form
    val isAddEditDialogOpen: Boolean = false,
    val editingYearMonth: String? = null,
    val inputYearMonth: String = DateFormatterUtil.currentMonthKey(),
    val inputCardCost: String = "",
    val formError: String? = null,
    val membershipToDelete: MonthlyMembership? = null,

    // Export Dialog State
    val isExportDialogOpen: Boolean = false,
    val exportFormat: ExportFormat = ExportFormat.JSON,
    val exportedDataText: String = "",

    // Import Dialog State
    val isImportDialogOpen: Boolean = false,
    val importText: String = "",
    val importReplaceMode: Boolean = false,
    val importError: String? = null,

    // Feedback message
    val userMessage: String? = null
)

class SubscriptionsViewModel(
    private val repository: MultisportRepository
) : ViewModel() {

    private data class FormState(
        val isEditDefaultCostDialogOpen: Boolean = false,
        val inputDefaultCardCost: String = "",
        val defaultCostError: String? = null,

        val isAddEditDialogOpen: Boolean = false,
        val editingYearMonth: String? = null,
        val inputYearMonth: String = DateFormatterUtil.currentMonthKey(),
        val inputCardCost: String = "",
        val formError: String? = null,
        val membershipToDelete: MonthlyMembership? = null,

        val isExportDialogOpen: Boolean = false,
        val exportFormat: ExportFormat = ExportFormat.JSON,
        val exportedDataText: String = "",

        val isImportDialogOpen: Boolean = false,
        val importText: String = "",
        val importReplaceMode: Boolean = false,
        val importError: String? = null,

        val userMessage: String? = null
    )

    private val _formState = MutableStateFlow(FormState())

    val uiState: StateFlow<SubscriptionsUiState> = combine(
        repository.getAllMemberships(),
        repository.getDefaultCardCost(),
        _formState
    ) { memberships, defaultCardCost, form ->
        SubscriptionsUiState(
            defaultCardCost = defaultCardCost,
            memberships = memberships.sortedByDescending { it.yearMonth },

            isEditDefaultCostDialogOpen = form.isEditDefaultCostDialogOpen,
            inputDefaultCardCost = form.inputDefaultCardCost,
            defaultCostError = form.defaultCostError,

            isAddEditDialogOpen = form.isAddEditDialogOpen,
            editingYearMonth = form.editingYearMonth,
            inputYearMonth = form.inputYearMonth,
            inputCardCost = form.inputCardCost,
            formError = form.formError,
            membershipToDelete = form.membershipToDelete,

            isExportDialogOpen = form.isExportDialogOpen,
            exportFormat = form.exportFormat,
            exportedDataText = form.exportedDataText,

            isImportDialogOpen = form.isImportDialogOpen,
            importText = form.importText,
            importReplaceMode = form.importReplaceMode,
            importError = form.importError,

            userMessage = form.userMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubscriptionsUiState()
    )

    fun userMessageShown() {
        _formState.update { it.copy(userMessage = null) }
    }

    // --- Default Card Cost Actions ---

    fun openEditDefaultCostDialog() {
        viewModelScope.launch {
            val currentDefault = repository.getDefaultCardCostDirect()
            val formatted = if (currentDefault % 1.0 == 0.0) {
                currentDefault.toInt().toString()
            } else {
                String.format(Locale.US, "%.2f", currentDefault)
            }
            _formState.update {
                it.copy(
                    isEditDefaultCostDialogOpen = true,
                    inputDefaultCardCost = formatted,
                    defaultCostError = null
                )
            }
        }
    }

    fun closeEditDefaultCostDialog() {
        _formState.update { it.copy(isEditDefaultCostDialogOpen = false, defaultCostError = null) }
    }

    fun updateInputDefaultCost(cost: String) {
        _formState.update { it.copy(inputDefaultCardCost = cost, defaultCostError = null) }
    }

    fun saveDefaultCardCost() {
        val costDouble = _formState.value.inputDefaultCardCost.trim().toDoubleOrNull()
        if (costDouble == null || costDouble < 0) {
            _formState.update { it.copy(defaultCostError = "Zadejte platnou částku (např. 500).") }
            return
        }

        viewModelScope.launch {
            repository.setDefaultCardCost(costDouble)
            _formState.update {
                it.copy(
                    isEditDefaultCostDialogOpen = false,
                    defaultCostError = null,
                    userMessage = "Výchozí cena karty byla změněna na ${CurrencyUtil.formatKcz(costDouble)}"
                )
            }
        }
    }

    // --- Subscriptions Actions ---

    fun openAddSubscriptionDialog(defaultYearMonth: String? = null) {
        val ym = defaultYearMonth ?: DateFormatterUtil.currentMonthKey()
        viewModelScope.launch {
            val defaultCost = repository.getDefaultCardCostDirect()
            val formattedCost = if (defaultCost % 1.0 == 0.0) {
                defaultCost.toInt().toString()
            } else {
                String.format(Locale.US, "%.2f", defaultCost)
            }
            _formState.update {
                it.copy(
                    isAddEditDialogOpen = true,
                    editingYearMonth = null,
                    inputYearMonth = ym,
                    inputCardCost = formattedCost,
                    formError = null
                )
            }
        }
    }

    fun openEditSubscriptionDialog(membership: MonthlyMembership) {
        val formattedCost = if (membership.cardCost % 1.0 == 0.0) {
            membership.cardCost.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", membership.cardCost)
        }
        _formState.update {
            it.copy(
                isAddEditDialogOpen = true,
                editingYearMonth = membership.yearMonth,
                inputYearMonth = membership.yearMonth,
                inputCardCost = formattedCost,
                formError = null
            )
        }
    }

    fun closeAddEditSubscriptionDialog() {
        _formState.update { it.copy(isAddEditDialogOpen = false, formError = null) }
    }

    fun updateInputYearMonth(yearMonth: String) {
        _formState.update { it.copy(inputYearMonth = yearMonth, formError = null) }
    }

    fun updateInputCardCost(cost: String) {
        _formState.update { it.copy(inputCardCost = cost, formError = null) }
    }

    fun saveSubscription() {
        val state = _formState.value
        val ym = state.inputYearMonth.trim()
        val costText = state.inputCardCost.trim()

        if (!DateFormatterUtil.isValidYearMonth(ym)) {
            _formState.update { it.copy(formError = "Neplatný formát měsíce. Použijte RRRR-MM (např. 2025-05).") }
            return
        }

        val costDouble = costText.toDoubleOrNull()
        if (costDouble == null || costDouble < 0) {
            _formState.update { it.copy(formError = "Zadejte platnou částku (např. 500).") }
            return
        }

        viewModelScope.launch {
            repository.saveMembership(MonthlyMembership(yearMonth = ym, cardCost = costDouble))
            _formState.update {
                it.copy(
                    isAddEditDialogOpen = false,
                    formError = null,
                    userMessage = "Uložena cena předplatného pro ${DateFormatterUtil.formatToMonthYear(ym)}"
                )
            }
        }
    }

    fun promptDeleteSubscription(membership: MonthlyMembership) {
        _formState.update { it.copy(membershipToDelete = membership) }
    }

    fun dismissDeleteSubscription() {
        _formState.update { it.copy(membershipToDelete = null) }
    }

    fun confirmDeleteSubscription() {
        val membership = _formState.value.membershipToDelete ?: return
        viewModelScope.launch {
            repository.deleteMembership(membership)
            _formState.update {
                it.copy(
                    membershipToDelete = null,
                    userMessage = "Předplatné pro ${DateFormatterUtil.formatToMonthYear(membership.yearMonth)} bylo smazáno"
                )
            }
        }
    }

    // --- Export Actions ---

    fun openExportDialog(format: ExportFormat = ExportFormat.JSON) {
        viewModelScope.launch {
            val logs = repository.getAllLogsList()
            val templates = repository.getAllTemplatesList()
            val memberships = repository.getAllMembershipsList()

            val text = if (format == ExportFormat.JSON) {
                DataExportImportUtil.exportToJson(logs, templates, memberships)
            } else {
                DataExportImportUtil.exportToCsv(logs, templates, memberships)
            }

            _formState.update {
                it.copy(
                    isExportDialogOpen = true,
                    exportFormat = format,
                    exportedDataText = text
                )
            }
        }
    }

    fun setExportFormat(format: ExportFormat) {
        openExportDialog(format)
    }

    fun closeExportDialog() {
        _formState.update { it.copy(isExportDialogOpen = false) }
    }

    suspend fun generateExportData(format: ExportFormat): String {
        val logs = repository.getAllLogsList()
        val templates = repository.getAllTemplatesList()
        val memberships = repository.getAllMembershipsList()

        return if (format == ExportFormat.JSON) {
            DataExportImportUtil.exportToJson(logs, templates, memberships)
        } else {
            DataExportImportUtil.exportToCsv(logs, templates, memberships)
        }
    }

    // --- Import Actions ---

    fun openImportDialog() {
        _formState.update {
            it.copy(
                isImportDialogOpen = true,
                importText = "",
                importReplaceMode = false,
                importError = null
            )
        }
    }

    fun closeImportDialog() {
        _formState.update { it.copy(isImportDialogOpen = false, importError = null) }
    }

    fun updateImportText(text: String) {
        _formState.update { it.copy(importText = text, importError = null) }
    }

    fun updateImportReplaceMode(replace: Boolean) {
        _formState.update { it.copy(importReplaceMode = replace) }
    }

    fun executeImport(rawContent: String? = null) {
        val content = (rawContent ?: _formState.value.importText).trim()
        if (content.isBlank()) {
            _formState.update { it.copy(importError = "Vložte nebo vyberte data k importu.") }
            return
        }

        viewModelScope.launch {
            try {
                val data = DataExportImportUtil.importData(content)
                val replace = _formState.value.importReplaceMode

                if (replace) {
                    repository.deleteAllLogs()
                    repository.deleteAllTemplates()
                    repository.deleteAllMemberships()
                }

                if (data.activityLogs.isNotEmpty()) {
                    repository.insertAllLogs(data.activityLogs)
                }
                if (data.templates.isNotEmpty()) {
                    repository.insertAllTemplates(data.templates)
                }
                if (data.monthlyMemberships.isNotEmpty()) {
                    repository.insertAllMemberships(data.monthlyMemberships)
                }

                val msg = "Úspěšně importováno: ${data.activityLogs.size} záznamů, ${data.templates.size} šablon, ${data.monthlyMemberships.size} předplatných!"

                _formState.update {
                    it.copy(
                        isImportDialogOpen = false,
                        importText = "",
                        importError = null,
                        userMessage = msg
                    )
                }
            } catch (e: Exception) {
                _formState.update { it.copy(importError = "Import se nezdařil: ${e.localizedMessage ?: "Neplatná struktura JSON nebo CSV."}") }
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
                return SubscriptionsViewModel(repository) as T
            }
        }
    }
}
