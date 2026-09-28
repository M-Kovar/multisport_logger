package com.example.multisport_logger.ui.subscriptions

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.multisport_logger.R
import com.example.multisport_logger.data.entity.MonthlyMembership
import com.example.multisport_logger.ui.activitylog.DateFormatterUtil
import com.example.multisport_logger.ui.theme.Multisport_loggerTheme
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun SubscriptionsExportScreen(
    viewModel: SubscriptionsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.userMessageShown()
        }
    }

    // SAF Export File Creation Launcher
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val content = viewModel.generateExportData(uiState.exportFormat)
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(content.toByteArray())
                    }
                    snackbarHostState.showSnackbar("Export byl úspěšně uložen do souboru!")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Nepodařilo se uložit soubor: ${e.localizedMessage}")
                }
            }
        }
    }

    // SAF Import Open File Launcher
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val content = reader.readText()
                    viewModel.executeImport(content)
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Nepodařilo se načíst soubor: ${e.localizedMessage}")
                }
            }
        }
    }

    SubscriptionsExportContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onOpenEditDefaultCost = viewModel::openEditDefaultCostDialog,
        onCloseEditDefaultCost = viewModel::closeEditDefaultCostDialog,
        onDefaultCostChange = viewModel::updateInputDefaultCost,
        onSaveDefaultCost = viewModel::saveDefaultCardCost,

        onOpenAddSubscription = { viewModel.openAddSubscriptionDialog() },
        onOpenEditSubscription = viewModel::openEditSubscriptionDialog,
        onDeleteSubscriptionPrompt = viewModel::promptDeleteSubscription,
        onDismissDeleteSubscription = viewModel::dismissDeleteSubscription,
        onConfirmDeleteSubscription = viewModel::confirmDeleteSubscription,
        onCloseAddEditSubscription = viewModel::closeAddEditSubscriptionDialog,
        onYearMonthChange = viewModel::updateInputYearMonth,
        onCardCostChange = viewModel::updateInputCardCost,
        onSaveSubscription = viewModel::saveSubscription,

        onOpenExportDialog = { viewModel.openExportDialog(ExportFormat.JSON) },
        onCloseExportDialog = viewModel::closeExportDialog,
        onExportFormatChange = viewModel::setExportFormat,
        onShareExport = { text, format ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (format == ExportFormat.JSON) "application/json" else "text/csv"
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_SUBJECT, "Záloha dat MultiSport Logger")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Sdílet data MultiSport"))
        },
        onSaveExportToFile = { format ->
            val fileName = if (format == ExportFormat.JSON) "multisport_zaloha.json" else "multisport_zaloha.csv"
            createDocumentLauncher.launch(fileName)
        },

        onOpenImportDialog = viewModel::openImportDialog,
        onCloseImportDialog = viewModel::closeImportDialog,
        onImportTextChange = viewModel::updateImportText,
        onImportReplaceModeChange = viewModel::updateImportReplaceMode,
        onPickImportFile = {
            openDocumentLauncher.launch(arrayOf("application/json", "text/csv", "text/plain", "*/*"))
        },
        onExecuteImport = { viewModel.executeImport() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsExportContent(
    uiState: SubscriptionsUiState,
    snackbarHostState: SnackbarHostState,
    onOpenEditDefaultCost: () -> Unit,
    onCloseEditDefaultCost: () -> Unit,
    onDefaultCostChange: (String) -> Unit,
    onSaveDefaultCost: () -> Unit,

    onOpenAddSubscription: () -> Unit,
    onOpenEditSubscription: (MonthlyMembership) -> Unit,
    onDeleteSubscriptionPrompt: (MonthlyMembership) -> Unit,
    onDismissDeleteSubscription: () -> Unit,
    onConfirmDeleteSubscription: () -> Unit,
    onCloseAddEditSubscription: () -> Unit,
    onYearMonthChange: (String) -> Unit,
    onCardCostChange: (String) -> Unit,
    onSaveSubscription: () -> Unit,

    onOpenExportDialog: () -> Unit,
    onCloseExportDialog: () -> Unit,
    onExportFormatChange: (ExportFormat) -> Unit,
    onShareExport: (String, ExportFormat) -> Unit,
    onSaveExportToFile: (ExportFormat) -> Unit,

    onOpenImportDialog: () -> Unit,
    onCloseImportDialog: () -> Unit,
    onImportTextChange: (String) -> Unit,
    onImportReplaceModeChange: (Boolean) -> Unit,
    onPickImportFile: () -> Unit,
    onExecuteImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.title_subscriptions), fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddSubscription,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = stringResource(R.string.btn_add_month))
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Default Recurring Card Cost Section
            item {
                DefaultCardCostSection(
                    defaultCardCost = uiState.defaultCardCost,
                    onEditClick = onOpenEditDefaultCost
                )
            }

            // Monthly Subscriptions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.header_monthly_card_costs),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onOpenAddSubscription,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_add_month))
                    }
                }
            }

            // List of Monthly Subscriptions / Overrides
            if (uiState.memberships.isEmpty()) {
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.desc_no_subscriptions),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.desc_no_subscriptions_sub),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.memberships, key = { it.yearMonth }) { membership ->
                    SubscriptionCardItem(
                        membership = membership,
                        onEdit = { onOpenEditSubscription(membership) },
                        onDelete = { onDeleteSubscriptionPrompt(membership) }
                    )
                }
            }

            // Data Backup & Export/Import Section
            item {
                DataExportImportSection(
                    onOpenExport = onOpenExportDialog,
                    onOpenImport = onOpenImportDialog
                )
            }
        }
    }

    // Edit Default Card Cost Dialog
    if (uiState.isEditDefaultCostDialogOpen) {
        EditDefaultCostDialog(
            currentCost = uiState.inputDefaultCardCost,
            formError = uiState.defaultCostError,
            onCostChange = onDefaultCostChange,
            onDismiss = onCloseEditDefaultCost,
            onSave = onSaveDefaultCost
        )
    }

    // Add/Edit Monthly Subscription Override Dialog
    if (uiState.isAddEditDialogOpen) {
        AddEditSubscriptionDialog(
            isEditing = uiState.editingYearMonth != null,
            yearMonth = uiState.inputYearMonth,
            cardCost = uiState.inputCardCost,
            formError = uiState.formError,
            onYearMonthChange = onYearMonthChange,
            onCardCostChange = onCardCostChange,
            onDismiss = onCloseAddEditSubscription,
            onSave = onSaveSubscription
        )
    }

    // Delete Subscription Confirmation Dialog
    uiState.membershipToDelete?.let { membership ->
        AlertDialog(
            onDismissRequest = onDismissDeleteSubscription,
            title = { Text(stringResource(R.string.dialog_delete_subscription_title)) },
            text = { Text(stringResource(R.string.dialog_delete_subscription_msg, DateFormatterUtil.formatToMonthYear(membership.yearMonth))) },
            confirmButton = {
                TextButton(onClick = onConfirmDeleteSubscription) {
                    Text(stringResource(R.string.btn_delete), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteSubscription) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Export Data Dialog
    if (uiState.isExportDialogOpen) {
        ExportDataDialog(
            exportFormat = uiState.exportFormat,
            exportedText = uiState.exportedDataText,
            onFormatChange = onExportFormatChange,
            onShare = onShareExport,
            onSaveFile = onSaveExportToFile,
            onDismiss = onCloseExportDialog
        )
    }

    // Import Data Dialog
    if (uiState.isImportDialogOpen) {
        ImportDataDialog(
            importText = uiState.importText,
            importReplaceMode = uiState.importReplaceMode,
            importError = uiState.importError,
            onImportTextChange = onImportTextChange,
            onReplaceModeChange = onImportReplaceModeChange,
            onPickFile = onPickImportFile,
            onExecuteImport = onExecuteImport,
            onDismiss = onCloseImportDialog
        )
    }
}

@Composable
private fun DefaultCardCostSection(
    defaultCardCost: Double,
    onEditClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.CreditCard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = stringResource(R.string.header_default_card_cost),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.desc_default_card_cost),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = DateFormatterUtil.formatCurrency(defaultCardCost),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            FilledTonalButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.btn_edit))
            }
        }
    }
}

@Composable
private fun EditDefaultCostDialog(
    currentCost: String,
    formError: String?,
    onCostChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_edit_default_cost),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.desc_default_card_cost),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!formError.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formError,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = currentCost,
                    onValueChange = onCostChange,
                    label = { Text(stringResource(R.string.label_default_card_cost)) },
                    placeholder = { Text("500") },
                    suffix = { Text(stringResource(R.string.currency_suffix)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.CreditCard, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(stringResource(R.string.btn_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
private fun DataExportImportSection(
    onOpenExport: () -> Unit,
    onOpenImport: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.header_backup_transfer),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.desc_backup_transfer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onOpenExport,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_export_data))
                    }

                    OutlinedButton(
                        onClick = onOpenImport,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.btn_import_data))
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionCardItem(
    membership: MonthlyMembership,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = DateFormatterUtil.formatToMonthYear(membership.yearMonth),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = membership.yearMonth,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = DateFormatterUtil.formatCurrency(membership.cardCost),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.dialog_edit_subscription),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.dialog_delete_subscription_title),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubscriptionsExportScreenPreview() {
    Multisport_loggerTheme {
        SubscriptionsExportContent(
            uiState = SubscriptionsUiState(
                defaultCardCost = 500.0,
                memberships = listOf(
                    MonthlyMembership("2025-05", 500.0),
                    MonthlyMembership("2025-04", 450.0)
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onOpenEditDefaultCost = {},
            onCloseEditDefaultCost = {},
            onDefaultCostChange = {},
            onSaveDefaultCost = {},
            onOpenAddSubscription = {},
            onOpenEditSubscription = {},
            onDeleteSubscriptionPrompt = {},
            onDismissDeleteSubscription = {},
            onConfirmDeleteSubscription = {},
            onCloseAddEditSubscription = {},
            onYearMonthChange = {},
            onCardCostChange = {},
            onSaveSubscription = {},
            onOpenExportDialog = {},
            onCloseExportDialog = {},
            onExportFormatChange = {},
            onShareExport = { _, _ -> },
            onSaveExportToFile = {},
            onOpenImportDialog = {},
            onCloseImportDialog = {},
            onImportTextChange = {},
            onImportReplaceModeChange = {},
            onPickImportFile = {},
            onExecuteImport = {}
        )
    }
}
