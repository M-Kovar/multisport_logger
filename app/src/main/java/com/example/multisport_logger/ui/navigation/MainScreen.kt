package com.example.multisport_logger.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.multisport_logger.R
import com.example.multisport_logger.ui.activitylog.ActivityLogScreen
import com.example.multisport_logger.ui.activitylog.ActivityLogViewModel
import com.example.multisport_logger.ui.analytics.AnalyticsDashboardScreen
import com.example.multisport_logger.ui.analytics.AnalyticsViewModel
import com.example.multisport_logger.ui.subscriptions.SubscriptionsExportScreen
import com.example.multisport_logger.ui.subscriptions.SubscriptionsViewModel

enum class NavigationTab(
    @get:StringRes val titleRes: Int,
    val icon: ImageVector
) {
    LOGS(R.string.tab_activities, Icons.Rounded.FitnessCenter),
    ANALYTICS(R.string.tab_analytics, Icons.Rounded.Analytics),
    SUBSCRIPTIONS(R.string.tab_subscriptions, Icons.Rounded.CreditCard)
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext

    val activityLogViewModel: ActivityLogViewModel = viewModel(
        factory = ActivityLogViewModel.factory(context)
    )
    val analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = AnalyticsViewModel.factory(context)
    )
    val subscriptionsViewModel: SubscriptionsViewModel = viewModel(
        factory = SubscriptionsViewModel.factory(context)
    )

    var selectedTabOrdinal by remember { mutableIntStateOf(NavigationTab.LOGS.ordinal) }
    val selectedTab = NavigationTab.entries[selectedTabOrdinal]

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationTab.entries.forEach { tab ->
                    val title = stringResource(tab.titleRes)
                    NavigationBarItem(
                        selected = tab == selectedTab,
                        onClick = { selectedTabOrdinal = tab.ordinal },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = title
                            )
                        },
                        alwaysShowLabel = false
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavigationTab.LOGS -> {
                    ActivityLogScreen(viewModel = activityLogViewModel)
                }
                NavigationTab.ANALYTICS -> {
                    AnalyticsDashboardScreen(viewModel = analyticsViewModel)
                }
                NavigationTab.SUBSCRIPTIONS -> {
                    SubscriptionsExportScreen(viewModel = subscriptionsViewModel)
                }
            }
        }
    }
}
