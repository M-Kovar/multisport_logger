package com.example.multisport_logger.ui.analytics

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.multisport_logger.R
import com.example.multisport_logger.ui.activitylog.DateFormatterUtil
import com.example.multisport_logger.ui.theme.Multisport_loggerTheme
import kotlin.math.abs

@Composable
fun AnalyticsDashboardScreen(
    viewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnalyticsDashboardContent(
        uiState = uiState,
        onSelectMonth = viewModel::setSelectedMonth,
        onPreviousMonth = viewModel::selectPreviousMonth,
        onNextMonth = viewModel::selectNextMonth,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardContent(
    uiState: AnalyticsUiState,
    onSelectMonth: (String) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMonthDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_analytics),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Month Selector Bar
            item {
                MonthSelectorHeader(
                    selectedMonth = uiState.selectedMonth,
                    availableMonths = uiState.availableMonths,
                    isDropdownExpanded = isMonthDropdownExpanded,
                    onToggleDropdown = { isMonthDropdownExpanded = !isMonthDropdownExpanded },
                    onDismissDropdown = { isMonthDropdownExpanded = false },
                    onSelectMonth = {
                        onSelectMonth(it)
                        isMonthDropdownExpanded = false
                    },
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth
                )
            }

            // Current Month Overview Hero Card
            item {
                CurrentMonthOverviewCard(uiState = uiState)
            }

            // Current Month Metrics Grid (3 mini cards)
            item {
                CurrentMonthMetricsGrid(uiState = uiState)
            }

            // Top Venues in Selected Month
            if (uiState.topVenuesForMonth.isNotEmpty()) {
                item {
                    TopVenuesSection(topVenues = uiState.topVenuesForMonth)
                }
            }

            // All-Time Summary Section
            item {
                AllTimeSummaryCard(uiState = uiState)
            }
        }
    }
}

@Composable
private fun MonthSelectorHeader(
    selectedMonth: String,
    availableMonths: List<String>,
    isDropdownExpanded: Boolean,
    onToggleDropdown: () -> Unit,
    onDismissDropdown: () -> Unit,
    onSelectMonth: (String) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Předchozí měsíc"
                )
            }

            Box {
                FilterChip(
                    selected = true,
                    onClick = onToggleDropdown,
                    label = {
                        Text(
                            text = DateFormatterUtil.formatToMonthYear(selectedMonth),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = onDismissDropdown
                ) {
                    availableMonths.forEach { monthKey ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = DateFormatterUtil.formatToMonthYear(monthKey),
                                    fontWeight = if (monthKey == selectedMonth) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = { onSelectMonth(monthKey) }
                        )
                    }
                }
            }

            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Následující měsíc"
                )
            }
        }
    }
}

@Composable
private fun CurrentMonthOverviewCard(
    uiState: AnalyticsUiState
) {
    val isPositive = uiState.isMonthPositiveSavings
    val positiveGreen = Color(0xFF2E7D32)
    val positiveGreenContainer = Color(0xFFE8F5E9)
    val negativeRed = Color(0xFFC62828)
    val negativeRedContainer = Color(0xFFFFEBEE)

    val badgeBgColor = if (isPositive) positiveGreenContainer else negativeRedContainer
    val badgeTextColor = if (isPositive) positiveGreen else negativeRed
    val icon = if (isPositive) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown
    val titleText = if (isPositive) {
        stringResource(R.string.header_savings_this_month)
    } else {
        stringResource(R.string.header_net_spend)
    }

    val displaySavings = if (isPositive) {
        "+${DateFormatterUtil.formatCurrency(uiState.monthNetSavings)}"
    } else {
        "-${DateFormatterUtil.formatCurrency(abs(uiState.monthNetSavings))}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = badgeBgColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = badgeTextColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = badgeTextColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (uiState.monthCardCost > 0) {
                    val percentage = (uiState.monthCostRecoveryRatio * 100).toInt()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = badgeTextColor.copy(alpha = 0.18f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "$percentage % hodnoty",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = displaySavings,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = badgeTextColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (uiState.monthCardCost > 0.0) {
                val progress = uiState.monthCostRecoveryRatio.coerceIn(0f, 1f)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.header_break_even),
                            style = MaterialTheme.typography.bodySmall,
                            color = badgeTextColor.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${DateFormatterUtil.formatCurrency(uiState.monthActivityValue)} / ${DateFormatterUtil.formatCurrency(uiState.monthCardCost)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = badgeTextColor,
                        trackColor = badgeTextColor.copy(alpha = 0.2f)
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.tip_set_card_cost),
                    style = MaterialTheme.typography.bodySmall,
                    color = badgeTextColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun CurrentMonthMetricsGrid(
    uiState: AnalyticsUiState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricMiniCard(
            title = stringResource(R.string.header_visits),
            value = uiState.monthVisitCount.toString(),
            icon = Icons.Rounded.FitnessCenter,
            modifier = Modifier.weight(1f)
        )
        MetricMiniCard(
            title = stringResource(R.string.header_activity_value),
            value = DateFormatterUtil.formatCurrency(uiState.monthActivityValue),
            icon = Icons.Rounded.Paid,
            modifier = Modifier.weight(1f)
        )
        MetricMiniCard(
            title = stringResource(R.string.header_card_cost),
            value = DateFormatterUtil.formatCurrency(uiState.monthCardCost),
            icon = Icons.Rounded.CreditCard,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricMiniCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TopVenuesSection(
    topVenues: List<VenueVisitCount>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.header_top_venues),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topVenues.forEach { venue ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = venue.venueName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = DateFormatterUtil.formatVisits(venue.visitCount),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Text(
                                text = DateFormatterUtil.formatCurrency(venue.totalValue),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllTimeSummaryCard(
    uiState: AnalyticsUiState
) {
    val positiveGreen = Color(0xFF2E7D32)
    val negativeRed = Color(0xFFC62828)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.header_all_time_summary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // All time net savings banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (uiState.isAllTimePositiveSavings) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.header_all_time_savings),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (uiState.isAllTimePositiveSavings) positiveGreen else negativeRed
                        )
                        Text(
                            text = if (uiState.isAllTimePositiveSavings) {
                                "+${DateFormatterUtil.formatCurrency(uiState.allTimeNetSavings)}"
                            } else {
                                "-${DateFormatterUtil.formatCurrency(abs(uiState.allTimeNetSavings))}"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (uiState.isAllTimePositiveSavings) positiveGreen else negativeRed
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.Savings,
                        contentDescription = null,
                        tint = if (uiState.isAllTimePositiveSavings) positiveGreen else negativeRed,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Breakdown list
                AllTimeSummaryRow(
                    title = stringResource(R.string.header_total_visits),
                    value = DateFormatterUtil.formatVisits(uiState.allTimeVisitCount),
                    icon = Icons.Rounded.EventAvailable
                )

                AllTimeSummaryRow(
                    title = stringResource(R.string.header_total_activity_value),
                    value = DateFormatterUtil.formatCurrency(uiState.allTimeActivityValue),
                    icon = Icons.AutoMirrored.Rounded.DirectionsRun
                )

                AllTimeSummaryRow(
                    title = stringResource(R.string.header_total_subscriptions_paid),
                    value = DateFormatterUtil.formatCurrency(uiState.allTimeSubscriptionCost),
                    icon = Icons.Rounded.CreditCard
                )
            }
        }
    }
}

@Composable
private fun AllTimeSummaryRow(
    title: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsDashboardPreview() {
    Multisport_loggerTheme {
        AnalyticsDashboardContent(
            uiState = AnalyticsUiState(
                selectedMonth = "2025-05",
                defaultCardCost = 500.0,
                monthVisitCount = 8,
                monthActivityValue = 1200.0,
                monthCardCost = 500.0,
                monthNetSavings = 700.0,
                isMonthPositiveSavings = true,
                monthCostRecoveryRatio = 2.4f,
                topVenuesForMonth = listOf(
                    VenueVisitCount("Fit Gym Central", 5, 750.0),
                    VenueVisitCount("Olympic Pool", 3, 450.0)
                ),
                allTimeVisitCount = 35,
                allTimeActivityValue = 5250.0,
                allTimeSubscriptionCost = 2500.0,
                allTimeNetSavings = 2750.0,
                isAllTimePositiveSavings = true
            ),
            onSelectMonth = {},
            onPreviousMonth = {},
            onNextMonth = {}
        )
    }
}
