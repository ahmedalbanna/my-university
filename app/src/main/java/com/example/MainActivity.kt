package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.JamaatyApp
import com.example.ui.theme.JamaatyTheme

class MainActivity : ComponentActivity() {

    private var initialDeepLinkUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initialDeepLinkUri = intent?.data

        setContent {
            JamaatyTheme {
                JamaatyApp(
                    initialDeepLinkUri = initialDeepLinkUri,
                    onDeepLinkHandled = { initialDeepLinkUri = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        initialDeepLinkUri = intent.data
    }
}
