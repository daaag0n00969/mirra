package dev.dag0n.mirra.mirror

import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.os.IBinder
import android.view.WindowInsets
import android.widget.Toast
import dev.dag0n.mirra.MirraApp
import dev.dag0n.mirra.R
import dev.dag0n.mirra.data.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MirrorService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var settingsStore: SettingsStore
    private lateinit var capture: CaptureEngine
    private var overlay: OverlayController? = null
    private var bubble: BubbleController? = null
    private var settingsJob: Job? = null
    private var cropTop = 0
    private var cropBottom = 0
    private var cropLeft = 0
    private var cropRight = 0
    private var sessionActive = false

    private val a11yReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            recreateWindows()
        }
    }

    override fun onCreate() {
        super.onCreate()
        settingsStore = (application as MirraApp).settings
        Notifications.ensureChannel(this)
        capture = CaptureEngine(this) { stopSelf() }
        registerReceiver(a11yReceiver, IntentFilter(ACTION_A11Y_CHANGED), RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE -> {
                if (!sessionActive) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                toggleOverlay()
                return START_NOT_STICKY
            }
            ACTION_START, null -> {
                val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                    ?: return START_NOT_STICKY
                val data = intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                    ?: return START_NOT_STICKY
                cropTop = intent.getIntExtra(EXTRA_CROP_TOP, 0)
                cropBottom = intent.getIntExtra(EXTRA_CROP_BOTTOM, 0)
                cropLeft = intent.getIntExtra(EXTRA_CROP_LEFT, 0)
                cropRight = intent.getIntExtra(EXTRA_CROP_RIGHT, 0)
                startMirror(resultCode, data)
                return START_NOT_STICKY
            }
            else -> return START_NOT_STICKY
        }
    }

    private fun startMirror(resultCode: Int, data: Intent) {
        startForeground(
            Notifications.NOTIFICATION_ID,
            Notifications.build(this, overlayVisible = true),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION,
        )
        if (!capture.start(resultCode, data)) {
            Toast.makeText(this, R.string.error_projection, Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }
        sessionActive = true
        MirrorState.setRunning(true)
        val delaySec = settingsStore.current().startDelaySeconds
        scope.launch {
            if (delaySec > 0) {
                MirrorState.setStatus(getString(R.string.status_delay, delaySec))
                delay(delaySec * 1000L)
            }
            MirrorState.setStatus("")
            showWindows()
        }
        settingsJob?.cancel()
        settingsJob = scope.launch {
            settingsStore.value.collectLatest { settings ->
                overlay?.applySettings(settings)
                if (settings.showBubble) {
                    if (bubble == null && overlay != null) attachBubble()
                } else {
                    bubble?.detach()
                    bubble = null
                }
            }
        }
    }

    private fun showWindows() {
        if (overlay != null) return
        overlay = OverlayController(
            serviceContext = this,
            settingsStore = settingsStore,
            capture = capture,
            cropTop = cropTop,
            cropBottom = cropBottom,
            cropLeft = cropLeft,
            cropRight = cropRight,
        ).also { it.attach() }
        if (settingsStore.current().showBubble) attachBubble()
        refreshNotification(true)
    }

    private fun attachBubble() {
        if (bubble != null) return
        bubble = BubbleController(
            serviceContext = this,
            settingsStore = settingsStore,
            onToggleOverlay = { toggleOverlay() },
            onStop = { stopSelf() },
            onFlipHorizontal = {
                settingsStore.update { it.copy(flipHorizontal = !it.flipHorizontal) }
            },
        ).also {
            it.attach()
            it.setOverlayActive(overlay?.isVisible() != false)
        }
    }

    private fun toggleOverlay() {
        val shown = overlay?.toggle() ?: return
        bubble?.setOverlayActive(shown)
        refreshNotification(shown)
        MirrorState.setStatus(
            if (shown) getString(R.string.status_overlay_on)
            else getString(R.string.status_overlay_off)
        )
    }

    private fun recreateWindows() {
        if (!MirrorState.running.value) return
        val wasVisible = overlay?.isVisible() != false
        overlay?.detach()
        bubble?.detach()
        overlay = null
        bubble = null
        showWindows()
        if (!wasVisible) {
            overlay?.setVisible(false)
            bubble?.setOverlayActive(false)
            refreshNotification(false)
        }
    }

    private fun refreshNotification(overlayVisible: Boolean) {
        val manager = getSystemService(android.app.NotificationManager::class.java)
        manager.notify(Notifications.NOTIFICATION_ID, Notifications.build(this, overlayVisible))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        overlay?.applySettings(settingsStore.current())
    }

    override fun onDestroy() {
        settingsJob?.cancel()
        scope.cancel()
        runCatching { unregisterReceiver(a11yReceiver) }
        overlay?.detach()
        bubble?.detach()
        overlay = null
        bubble = null
        capture.stop()
        sessionActive = false
        MirrorState.setRunning(false)
        MirrorState.setStatus("")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "dev.dag0n.mirra.action.START"
        const val ACTION_STOP = "dev.dag0n.mirra.action.STOP"
        const val ACTION_TOGGLE = "dev.dag0n.mirra.action.TOGGLE"
        const val ACTION_A11Y_CHANGED = "dev.dag0n.mirra.action.A11Y_CHANGED"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
        const val EXTRA_CROP_TOP = "crop_top"
        const val EXTRA_CROP_BOTTOM = "crop_bottom"
        const val EXTRA_CROP_LEFT = "crop_left"
        const val EXTRA_CROP_RIGHT = "crop_right"

        fun start(
            context: Context,
            resultCode: Int,
            data: Intent,
            insets: WindowInsets?,
        ) {
            val bars = insets?.getInsets(WindowInsets.Type.systemBars())
            val intent = Intent(context, MirrorService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_RESULT_DATA, data)
                putExtra(EXTRA_CROP_TOP, bars?.top ?: 0)
                putExtra(EXTRA_CROP_BOTTOM, bars?.bottom ?: 0)
                putExtra(EXTRA_CROP_LEFT, bars?.left ?: 0)
                putExtra(EXTRA_CROP_RIGHT, bars?.right ?: 0)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, MirrorService::class.java).setAction(ACTION_STOP))
        }
    }
}
