package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import com.example.data.HealthDatabase
import com.example.data.HealthRepository
import com.example.ui.HealthHomeScreen
import com.example.ui.HealthViewModel
import com.example.ui.theme.SeniorHealthTheme

class MainActivity : ComponentActivity() {

  private lateinit var viewModel: HealthViewModel
  private var dateChangeReceiver: BroadcastReceiver? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = HealthDatabase.getInstance(applicationContext)
    val repository = HealthRepository(database.dailyHealthDao())
    viewModel =
      ViewModelProvider(this, HealthViewModel.Factory(repository))[HealthViewModel::class.java]

    lifecycle.addObserver(
      LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
          viewModel.refreshDateIfNeeded()
        }
      }
    )

    val receiver =
      object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
          viewModel.refreshDateIfNeeded()
        }
      }
    val filter =
      IntentFilter().apply {
        addAction(Intent.ACTION_DATE_CHANGED)
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
      }
    ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    dateChangeReceiver = receiver

    setContent { SeniorHealthTheme { HealthHomeScreen(viewModel = viewModel) } }
  }

  override fun onDestroy() {
    dateChangeReceiver?.let { unregisterReceiver(it) }
    dateChangeReceiver = null
    super.onDestroy()
  }
}
