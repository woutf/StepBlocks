package com.stepblocks

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.stepblocks.data.AppDatabase
import com.stepblocks.data.DailyProgress
import com.stepblocks.navigation.AppNavigation
import com.stepblocks.ui.theme.StepBlocksTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Midnight reset logic
        CoroutineScope(Dispatchers.IO).launch {
            val today = LocalDate.now()
            val date = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant())
            val db = AppDatabase.getDatabase(applicationContext)
            val dailyProgressDao = db.dailyProgressDao()
            val dailyProgress = dailyProgressDao.getDailyProgressByDate(date)
            if (dailyProgress == null) {
                dailyProgressDao.insertDailyProgress(
                    DailyProgress(date = date, templateId = "", blockProgress = emptyList(), lastStepTotal = 0L)
                )
            }
        }

        setContent {
            StepBlocksTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
        Log.d("DEBUG", "MainActivity onCreate called")
    }
}
