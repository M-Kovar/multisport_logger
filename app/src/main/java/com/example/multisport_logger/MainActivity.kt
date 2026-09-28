package com.example.multisport_logger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.multisport_logger.data.db.AppDatabase
import com.example.multisport_logger.data.repository.MultisportRepositoryImpl
import com.example.multisport_logger.ui.navigation.MainScreen
import com.example.multisport_logger.ui.theme.Multisport_loggerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure recurring monthly card payment exists for the current month
        val db = AppDatabase.getDatabase(applicationContext)
        val prefs = getSharedPreferences("multisport_prefs", MODE_PRIVATE)
        val repository = MultisportRepositoryImpl(
            activityDao = db.activityDao(),
            templateDao = db.templateDao(),
            membershipDao = db.membershipDao(),
            sharedPreferences = prefs
        )
        lifecycleScope.launch {
            repository.ensureCurrentMonthMembership()
        }

        setContent {
            Multisport_loggerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }
}
