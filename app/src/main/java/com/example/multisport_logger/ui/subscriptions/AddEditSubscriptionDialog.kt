package com.example.multisport_logger.ui.subscriptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.multisport_logger.R
import com.example.multisport_logger.ui.activitylog.DateFormatterUtil

@Composable
fun AddEditSubscriptionDialog(
    isEditing: Boolean,
    yearMonth: String,
    cardCost: String,
    formError: String?,
    onYearMonthChange: (String) -> Unit,
    onCardCostChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(if (isEditing) R.string.dialog_edit_subscription else R.string.dialog_add_subscription),
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
                    value = yearMonth,
                    onValueChange = onYearMonthChange,
                    label = { Text(stringResource(R.string.label_month_format)) },
                    placeholder = { Text("např. 2025-05") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.CalendarMonth, contentDescription = null)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick month chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currentMonth = DateFormatterUtil.currentMonthKey()
                    val previousMonth = DateFormatterUtil.previousMonthKey(currentMonth)

                    AssistChip(
                        onClick = { onYearMonthChange(currentMonth) },
                        label = { Text(stringResource(R.string.btn_this_month)) }
                    )
                    AssistChip(
                        onClick = { onYearMonthChange(previousMonth) },
                        label = { Text(stringResource(R.string.btn_last_month)) }
                    )
                }

                OutlinedTextField(
                    value = cardCost,
                    onValueChange = onCardCostChange,
                    label = { Text(stringResource(R.string.label_card_cost)) },
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
        },
        modifier = modifier
    )
}
