package com.example.multisport_logger.ui.activitylog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.multisport_logger.R
import com.example.multisport_logger.ui.theme.Multisport_loggerTheme

@Composable
fun AddEditTemplateDialog(
    isOpen: Boolean,
    editingTemplateId: Long?,
    venueName: String,
    activityName: String,
    defaultCost: String,
    errorMessage: String?,
    onVenueChange: (String) -> Unit,
    onActivityChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(if (editingTemplateId == null) R.string.dialog_new_template else R.string.dialog_edit_template),
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
                Text(
                    text = stringResource(R.string.desc_templates_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Venue Name
                OutlinedTextField(
                    value = venueName,
                    onValueChange = onVenueChange,
                    label = { Text(stringResource(R.string.label_venue_name)) },
                    placeholder = { Text("např. Metro Gym") },
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
                    placeholder = { Text("např. Plavání, Pilates") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.FitnessCenter,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Cost
                OutlinedTextField(
                    value = defaultCost,
                    onValueChange = onCostChange,
                    label = { Text(stringResource(R.string.label_default_cost)) },
                    placeholder = { Text("150") },
                    suffix = { Text(stringResource(R.string.currency_suffix)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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
                Text(stringResource(if (editingTemplateId == null) R.string.btn_create_template else R.string.btn_update_template))
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
fun AddEditTemplateDialogPreview() {
    Multisport_loggerTheme {
        AddEditTemplateDialog(
            isOpen = true,
            editingTemplateId = null,
            venueName = "Big1Fitness",
            activityName = "Posilovna",
            defaultCost = "160",
            errorMessage = null,
            onVenueChange = {},
            onActivityChange = {},
            onCostChange = {},
            onDismiss = {},
            onSave = {}
        )
    }
}
