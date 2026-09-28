package com.example.multisport_logger.ui.activitylog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.multisport_logger.R
import com.example.multisport_logger.ui.theme.Multisport_loggerTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditActivityLogDialog(
    isOpen: Boolean,
    editingLogId: Long?,
    date: String,
    venueName: String,
    activityName: String,
    cost: String,
    saveAsTemplate: Boolean,
    errorMessage: String?,
    onDateChange: (String) -> Unit,
    onVenueChange: (String) -> Unit,
    onActivityChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onSaveAsTemplateChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    if (!isOpen) return

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val initialMillis = try {
            val ld = LocalDate.parse(date.take(10))
            ld.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                                .format(DateTimeFormatter.ISO_LOCAL_DATE)
                            onDateChange(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.btn_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(if (editingLogId == null) R.string.dialog_log_visit else R.string.dialog_edit_visit),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date input field with picker button
                OutlinedTextField(
                    value = date,
                    onValueChange = onDateChange,
                    label = { Text(stringResource(R.string.label_date)) },
                    placeholder = { Text("2025-05-15") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = "Vybrat datum"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = "Otevřít výběr data"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Venue Name
                OutlinedTextField(
                    value = venueName,
                    onValueChange = onVenueChange,
                    label = { Text(stringResource(R.string.label_venue_name)) },
                    placeholder = { Text("např. Form Factory") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Place,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Activity Name
                OutlinedTextField(
                    value = activityName,
                    onValueChange = onActivityChange,
                    label = { Text(stringResource(R.string.label_activity_name)) },
                    placeholder = { Text("např. Posilovna, Plavání, Jóga") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.FitnessCenter,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Cost input field
                OutlinedTextField(
                    value = cost,
                    onValueChange = onCostChange,
                    label = { Text(stringResource(R.string.label_cost)) },
                    placeholder = { Text("150") },
                    suffix = { Text(stringResource(R.string.currency_suffix)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Save as template checkbox (when adding a new log)
                if (editingLogId == null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = saveAsTemplate,
                            onCheckedChange = onSaveAsTemplateChange
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.label_save_as_template),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(stringResource(if (editingLogId == null) R.string.btn_save_log else R.string.btn_update_log))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AddEditActivityLogDialogPreview() {
    Multisport_loggerTheme {
        AddEditActivityLogDialog(
            isOpen = true,
            editingLogId = null,
            date = "2025-05-15",
            venueName = "Form Factory Vinohradská",
            activityName = "Posilovna",
            cost = "180",
            saveAsTemplate = true,
            errorMessage = null,
            onDateChange = {},
            onVenueChange = {},
            onActivityChange = {},
            onCostChange = {},
            onSaveAsTemplateChange = {},
            onDismiss = {},
            onSave = {}
        )
    }
}
