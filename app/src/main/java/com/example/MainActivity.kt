package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.game.GameMainNavigator
import com.example.ui.game.GameViewModel
import com.example.ui.game.GameNotificationManager
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize SoundManager with attribution context
    com.example.ui.game.SoundManager.initialize(this)

    val baseContextWithAttribution = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        createAttributionContext("default")
    } else {
        this
    }

    // Initialize notification system
    GameNotificationManager.createNotificationChannel(baseContextWithAttribution)

    // Request notification permissions for Android 13+ (Tiramisu)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
      }
    }

    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          val gameViewModel: GameViewModel = viewModel()
          GameMainNavigator(viewModel = gameViewModel)
        }
      }
    }
  }

  override fun onStop() {
    super.onStop()
    val baseContextWithAttribution = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        createAttributionContext("default")
    } else {
        this
    }
    // Trigger local reminder notification to play when app minimizes
    GameNotificationManager.triggerPlayReminderNotification(baseContextWithAttribution)
  }

  override fun getAttributionTag(): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      "default"
    } else {
      super.getAttributionTag()
    }
  }
}
