package dev.dag0n.mirra

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowInsets
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import dev.dag0n.mirra.data.SettingsStore
import dev.dag0n.mirra.mirror.MirrorService
import dev.dag0n.mirra.mirror.MirrorState
import dev.dag0n.mirra.ui.HelpScreen
import dev.dag0n.mirra.ui.HomeScreen
import dev.dag0n.mirra.ui.SettingsScreen
import dev.dag0n.mirra.ui.theme.MirraTheme

class MainActivity : ComponentActivity() {

    private lateinit var settingsStore: SettingsStore
    private var screen by mutableStateOf(Screen.Home)
    private var lastInsets: WindowInsets? = null

    private val overlayPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private val capturePermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                MirrorService.start(this, result.resultCode, result.data!!, lastInsets)
                moveTaskToBack(true)
            } else {
                Toast.makeText(this, R.string.error_projection_denied, Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        settingsStore = (application as MirraApp).settings
        window.decorView.setOnApplyWindowInsetsListener { _, insets ->
            lastInsets = insets
            insets
        }
        setContent {
            val settings by settingsStore.value.collectAsState()
            val running by MirrorState.running.collectAsState()
            val overlayVisible by MirrorState.overlayVisible.collectAsState()
            val status by MirrorState.status.collectAsState()
            MirraTheme {
                when (screen) {
                    Screen.Home -> HomeScreen(
                        settings = settings,
                        running = running,
                        overlayVisible = overlayVisible,
                        status = status,
                        overlayGranted = Settings.canDrawOverlays(this),
                        a11yEnabled = accessibilityEnabled(),
                        onStart = ::onStartClicked,
                        onStop = { MirrorService.stop(this) },
                        onToggle = {
                            startService(
                                Intent(this, MirrorService::class.java)
                                    .setAction(MirrorService.ACTION_TOGGLE)
                            )
                        },
                        onOpenOverlaySettings = ::openOverlaySettings,
                        onOpenA11ySettings = ::openA11ySettings,
                        onRequestNotifications = {
                            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        },
                        onOpenSettings = { screen = Screen.Settings },
                        onOpenHelp = { screen = Screen.Help },
                    )
                    Screen.Settings -> SettingsScreen(
                        settings = settings,
                        a11yEnabled = accessibilityEnabled(),
                        onBack = { screen = Screen.Home },
                        onChange = { settingsStore.update(it) },
                        onReset = { settingsStore.reset() },
                        onOpenA11y = ::openA11ySettings,
                    )
                    Screen.Help -> HelpScreen(onBack = { screen = Screen.Home })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recompose permission cards after returning from Settings.
        screen = screen
    }

    private fun onStartClicked() {
        if (!Settings.canDrawOverlays(this)) {
            openOverlaySettings()
            Toast.makeText(this, R.string.need_overlay, Toast.LENGTH_LONG).show()
            return
        }
        val manager = getSystemService(MediaProjectionManager::class.java)
        capturePermission.launch(manager.createScreenCaptureIntent())
    }

    private fun openOverlaySettings() {
        overlayPermission.launch(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"),
            )
        )
    }

    private fun openA11ySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun accessibilityEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return enabled.contains(packageName)
    }

    private enum class Screen { Home, Settings, Help }
}
