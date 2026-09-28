package com.example.multisport_logger.ui.activitylog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.multisport_logger.R
import com.example.multisport_logger.data.entity.ActivityLog
import com.example.multisport_logger.data.entity.ActivityTemplate
import com.example.multisport_logger.ui.theme.Multisport_loggerTheme
import java.util.Locale

@Composable
fun ActivityLogScreen(
    viewModel: ActivityLogViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.userMessageShown()
        }
    }

    ActivityLogContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSearchQueryChange = viewModel::setSearchQuery,
        onSortOrderChange = viewModel::setSortOrder,
        onGroupByChange = viewModel::setGroupBy,
        onMonthFilterChange = viewModel::setSelectedMonthFilter,
        onOpenAddLog = { viewModel.openAddLogDialog() },
        onQuickLogFromTemplate = viewModel::quickLogFromTemplate,
        onOpenTemplatesManager = viewModel::openTemplatesManager,
        onEditLog = viewModel::openEditLogDialog,
        onDeleteLogPrompt = viewModel::promptDeleteLog,
        onDismissDeleteLog = viewModel::dismissDeleteLog,
        onConfirmDeleteLog = viewModel::confirmDeleteLog,
        onCloseAddEditLog = viewModel::closeAddEditLogDialog,
        onLogDateChange = viewModel::updateLogDateField,
        onLogVenueChange = viewModel::updateLogVenueField,
        onLogActivityChange = viewModel::updateLogActivityField,
        onLogCostChange = viewModel::updateLogCostField,
        onSaveAsTemplateChange = viewModel::updateSaveAsTemplate,
        onSaveLog = viewModel::saveActivityLog,
        onCloseTemplatesManager = viewModel::closeTemplatesManager,
        onOpenAddTemplate = viewModel::openAddTemplateDialog,
        onEditTemplate = viewModel::openEditTemplateDialog,
        onDeleteTemplatePrompt = viewModel::promptDeleteTemplate,
        onDismissDeleteTemplate = viewModel::dismissDeleteTemplate,
        onConfirmDeleteTemplate = viewModel::confirmDeleteTemplate,
        onCloseAddEditTemplate = viewModel::closeAddEditTemplateDialog,
        onTemplateVenueChange = viewModel::updateTemplateVenueField,
        onTemplateActivityChange = viewModel::updateTemplateActivityField,
        onTemplateCostChange = viewModel::updateTemplateCostField,
        onSaveTemplate = viewModel::saveTemplate,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogContent(
    uiState: ActivityLogUiState,
    snackbarHostState: SnackbarHostState,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onGroupByChange: (GroupBy) -> Unit,
    onMonthFilterChange: (String?) -> Unit,
    onOpenAddLog: () -> Unit,
    onQuickLogFromTemplate: (ActivityTemplate) -> Unit,
    onOpenTemplatesManager: () -> Unit,
    onEditLog: (ActivityLog) -> Unit,
    onDeleteLogPrompt: (ActivityLog) -> Unit,
    onDismissDeleteLog: () -> Unit,
    onConfirmDeleteLog: () -> Unit,
    onCloseAddEditLog: () -> Unit,
    onLogDateChange: (String) -> Unit,
    onLogVenueChange: (String) -> Unit,
    onLogActivityChange: (String) -> Unit,
    onLogCostChange: (String) -> Unit,
    onSaveAsTemplateChange: (Boolean) -> Unit,
    onSaveLog: () -> Unit,
    onCloseTemplatesManager: () -> Unit,
    onOpenAddTemplate: () -> Unit,
    onEditTemplate: (ActivityTemplate) -> Unit,
    onDeleteTemplatePrompt: (ActivityTemplate) -> Unit,
    onDismissDeleteTemplate: () -> Unit,
    onConfirmDeleteTemplate: () -> Unit,
    onCloseAddEditTemplate: () -> Unit,
    onTemplateVenueChange: (String) -> Unit,
    onTemplateActivityChange: (String) -> Unit,
    onTemplateCostChange: (String) -> Unit,
    onSaveTemplate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_activity_log),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    AssistChip(
                        onClick = onOpenTemplatesManager,
                        label = { Text(stringResource(R.string.header_templates, uiState.templates.size)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.FlashOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBottomSheet = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.btn_log_activity)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Filter Section
            SearchAndFilterControls(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                sortOrder = uiState.sortOrder,
                onSortOrderChange = onSortOrderChange,
                groupBy = uiState.groupBy,
                onGroupByChange = onGroupByChange,
                selectedMonth = uiState.selectedMonthFilter,
                availableMonths = uiState.availableMonths,
                onMonthFilterChange = onMonthFilterChange
            )

            // Summary Banner
            SummaryBanner(
                totalCount = uiState.totalFilteredCount,
                totalCost = uiState.totalFilteredCost,
                isFiltered = uiState.searchQuery.isNotBlank() || uiState.selectedMonthFilter != null
            )

            // Log List / Empty State
            if (uiState.groupedLogs.isEmpty()) {
                EmptyLogsView(
                    hasFilters = uiState.searchQuery.isNotBlank() || uiState.selectedMonthFilter != null,
                    onClearFilters = {
                        onSearchQueryChange("")
                        onMonthFilterChange(null)
                    },
                    onAddLog = { showAddBottomSheet = true }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    uiState.groupedLogs.forEach { (header, logs) ->
                        if (header.isNotBlank()) {
                            item(key = "header_$header") {
                                GroupHeaderCard(
                                    headerTitle = header,
                                    count = logs.size,
                                    sumCost = logs.sumOf { it.cost }
                                )
                            }
                        }

                        items(logs, key = { it.id }) { log ->
                            ActivityLogItemCard(
                                log = log,
                                onEdit = { onEditLog(log) },
                                onDelete = { onDeleteLogPrompt(log) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    // Add / Edit Activity Dialog
    AddEditActivityLogDialog(
        isOpen = uiState.isAddEditLogDialogOpen,
        editingLogId = uiState.editingLogId,
        date = uiState.logDate,
        venueName = uiState.logVenueName,
        activityName = uiState.logActivityName,
        cost = uiState.logCost,
        saveAsTemplate = uiState.saveAsTemplate,
        errorMessage = uiState.logFormError,
        onDateChange = onLogDateChange,
        onVenueChange = onLogVenueChange,
        onActivityChange = onLogActivityChange,
        onCostChange = onLogCostChange,
        onSaveAsTemplateChange = onSaveAsTemplateChange,
        onDismiss = onCloseAddEditLog,
        onSave = onSaveLog
    )

    // Templates Manager Dialog
    TemplatesManagerDialog(
        isOpen = uiState.isTemplatesManagerOpen,
        templates = uiState.templates,
        onDismiss = onCloseTemplatesManager,
        onAddTemplateClick = onOpenAddTemplate,
        onQuickLog = onQuickLogFromTemplate,
        onUseInForm = { template ->
            onCloseTemplatesManager()
            onLogVenueChange(template.venueName)
            onLogActivityChange(template.activityName)
            onLogCostChange(
                if (template.defaultCost % 1.0 == 0.0) template.defaultCost.toInt().toString() else String.format(Locale.US, "%.2f", template.defaultCost)
            )
            onOpenAddLog()
        },
        onEditTemplate = onEditTemplate,
        onDeleteTemplate = onDeleteTemplatePrompt
    )

    // Add / Edit Template Dialog
    AddEditTemplateDialog(
        isOpen = uiState.isAddEditTemplateDialogOpen,
        editingTemplateId = uiState.editingTemplateId,
        venueName = uiState.templateVenueName,
        activityName = uiState.templateActivityName,
        defaultCost = uiState.templateDefaultCost,
        errorMessage = uiState.templateFormError,
        onVenueChange = onTemplateVenueChange,
        onActivityChange = onTemplateActivityChange,
        onCostChange = onTemplateCostChange,
        onDismiss = onCloseAddEditTemplate,
        onSave = onSaveTemplate
    )

    // Delete Log Confirmation Dialog
    uiState.logToDelete?.let { log ->
        AlertDialog(
            onDismissRequest = onDismissDeleteLog,
            title = { Text(stringResource(R.string.dialog_delete_log_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.dialog_delete_log_msg,
                        log.activityName,
                        log.venueName,
                        DateFormatterUtil.formatToDisplay(log.date)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDeleteLog) {
                    Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteLog) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Delete Template Confirmation Dialog
    uiState.templateToDelete?.let { template ->
        AlertDialog(
            onDismissRequest = onDismissDeleteTemplate,
            title = { Text(stringResource(R.string.dialog_delete_template_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.dialog_delete_template_msg,
                        template.activityName,
                        template.venueName
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDeleteTemplate) {
                    Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteTemplate) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Modal Bottom Sheet for Add Activity & Quick Templates
    if (showAddBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Záznam aktivity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Action item: 'Nová aktivita' (opens the manual Add Activity dialog)
                Card(
                    onClick = {
                        showAddBottomSheet = false
                        onOpenAddLog()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Nová aktivita",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Zadat ručně místo, aktivitu a cenu",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // List of existing templates with 1-tap quick logging for today
                if (uiState.templates.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.FlashOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Rychlé šablony (dnes)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "${uiState.templates.size}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.templates, key = { it.id }) { template ->
                            OutlinedCard(
                                onClick = {
                                    showAddBottomSheet = false
                                    onQuickLogFromTemplate(template)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Rounded.FitnessCenter,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = template.venueName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = template.activityName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = DateFormatterUtil.formatCurrency(template.defaultCost),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Option to manage/add templates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showAddBottomSheet = false
                            onOpenAddTemplate()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Přidat šablonu")
                    }

                    OutlinedButton(
                        onClick = {
                            showAddBottomSheet = false
                            onOpenTemplatesManager()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Správa šablon")
                    }
                }
            }
        }
    }
}

// --- Sub-Components ---

@Composable
fun SearchAndFilterControls(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    groupBy: GroupBy,
    onGroupByChange: (GroupBy) -> Unit,
    selectedMonth: String?,
    availableMonths: List<String>,
    onMonthFilterChange: (String?) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showGroupMenu by remember { mutableStateOf(false) }
    var showMonthMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Streamlined Compact Search Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Hledat...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = stringResource(R.string.label_clear_search),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Filter / Sort Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group By Chip
            item {
                Box {
                    FilterChip(
                        selected = groupBy != GroupBy.NONE,
                        onClick = { showGroupMenu = true },
                        label = { Text("${stringResource(R.string.header_group)}: ${groupBy.label}") },
                        leadingIcon = { Icon(Icons.Rounded.ViewAgenda, contentDescription = null) }
                    )
                    DropdownMenu(
                        expanded = showGroupMenu,
                        onDismissRequest = { showGroupMenu = false }
                    ) {
                        GroupBy.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    onGroupByChange(option)
                                    showGroupMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Sort Order Chip
            item {
                Box {
                    FilterChip(
                        selected = sortOrder != SortOrder.DATE_DESC,
                        onClick = { showSortMenu = true },
                        label = { Text("${stringResource(R.string.header_sort)}: ${sortOrder.label}") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = null) }
                    )
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOrder.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    onSortOrderChange(option)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Month Filter Chip
            if (availableMonths.isNotEmpty()) {
                item {
                    Box {
                        FilterChip(
                            selected = selectedMonth != null,
                            onClick = { showMonthMenu = true },
                            label = {
                                Text(
                                    selectedMonth?.let { DateFormatterUtil.formatToMonthYear(it) } ?: stringResource(R.string.header_all_months)
                                )
                            },
                            leadingIcon = { Icon(Icons.Rounded.FilterList, contentDescription = null) }
                        )
                        DropdownMenu(
                            expanded = showMonthMenu,
                            onDismissRequest = { showMonthMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.header_all_months)) },
                                onClick = {
                                    onMonthFilterChange(null)
                                    showMonthMenu = false
                                }
                            )
                            availableMonths.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(DateFormatterUtil.formatToMonthYear(m)) },
                                    onClick = {
                                        onMonthFilterChange(m)
                                        showMonthMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryBanner(
    totalCount: Int,
    totalCost: Double,
    isFiltered: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(if (isFiltered) R.string.header_filtered_visits else R.string.header_recorded_visits),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = DateFormatterUtil.formatVisits(totalCount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.header_total_spent),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = DateFormatterUtil.formatCurrency(totalCost),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun GroupHeaderCard(
    headerTitle: String,
    count: Int,
    sumCost: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = headerTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "${DateFormatterUtil.formatVisits(count)} • ${DateFormatterUtil.formatCurrency(sumCost)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ActivityLogItemCard(
    log: ActivityLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = DateFormatterUtil.formatToDisplay(log.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = log.venueName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = log.activityName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = DateFormatterUtil.formatCurrency(log.cost),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.btn_edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.btn_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyLogsView(
    hasFilters: Boolean,
    onClearFilters: () -> Unit,
    onAddLog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.FitnessCenter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(if (hasFilters) R.string.desc_no_match_filter else R.string.desc_no_activities),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(if (hasFilters) R.string.desc_no_match_filter_sub else R.string.desc_no_activities_sub),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (hasFilters) {
            TextButton(onClick = onClearFilters) {
                Text(stringResource(R.string.btn_clear_filters))
            }
        } else {
            Button(
                onClick = onAddLog,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.btn_log_first_activity))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityLogContentPreview() {
    Multisport_loggerTheme {
        ActivityLogContent(
            uiState = ActivityLogUiState(
                groupedLogs = mapOf(
                    "Květen 2025" to listOf(
                        ActivityLog(1, "2025-05-15", "Boulder Planet", "Bouldering", 180.0),
                        ActivityLog(2, "2025-05-12", "Městský bazén", "Plavání", 120.0)
                    )
                ),
                totalFilteredCost = 300.0,
                totalFilteredCount = 2,
                templates = listOf(
                    ActivityTemplate(1, "Boulder Planet", "Bouldering", 180.0)
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onSearchQueryChange = {},
            onSortOrderChange = {},
            onGroupByChange = {},
            onMonthFilterChange = {},
            onOpenAddLog = {},
            onQuickLogFromTemplate = {},
            onOpenTemplatesManager = {},
            onEditLog = {},
            onDeleteLogPrompt = {},
            onDismissDeleteLog = {},
            onConfirmDeleteLog = {},
            onCloseAddEditLog = {},
            onLogDateChange = {},
            onLogVenueChange = {},
            onLogActivityChange = {},
            onLogCostChange = {},
            onSaveAsTemplateChange = {},
            onSaveLog = {},
            onCloseTemplatesManager = {},
            onOpenAddTemplate = {},
            onEditTemplate = {},
            onDeleteTemplatePrompt = {},
            onDismissDeleteTemplate = {},
            onConfirmDeleteTemplate = {},
            onCloseAddEditTemplate = {},
            onTemplateVenueChange = {},
            onTemplateActivityChange = {},
            onTemplateCostChange = {},
            onSaveTemplate = {}
        )
    }
}
