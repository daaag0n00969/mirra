package dev.dag0n.mirra.mirror

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.view.Surface

class CaptureEngine(
    private val context: Context,
    private val onStopped: () -> Unit,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var surface: Surface? = null
    private var width = 0
    private var height = 0

    private val callback = object : MediaProjection.Callback() {
        override fun onStop() {
            onStopped()
        }

        override fun onCapturedContentResize(w: Int, h: Int) {
            if (w > 0 && h > 0) resize(w, h)
        }
    }

    fun start(resultCode: Int, data: Intent): Boolean {
        val manager = context.getSystemService(MediaProjectionManager::class.java)
        val mp = manager.getMediaProjection(resultCode, data) ?: return false
        mp.registerCallback(callback, handler)
        projection = mp
        return true
    }

    fun bindSurface(newSurface: Surface, w: Int, h: Int) {
        surface?.release()
        surface = newSurface
        width = w.coerceAtLeast(1)
        height = h.coerceAtLeast(1)
        val dpi = context.resources.displayMetrics.densityDpi
        val mp = projection ?: return
        display?.release()
        display = mp.createVirtualDisplay(
            DISPLAY_NAME,
            width,
            height,
            dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            newSurface,
            null,
            handler,
        )
    }

    fun resize(w: Int, h: Int) {
        width = w.coerceAtLeast(1)
        height = h.coerceAtLeast(1)
        display?.resize(width, height, context.resources.displayMetrics.densityDpi)
    }

    fun releaseDisplay() {
        display?.release()
        display = null
        surface?.release()
        surface = null
    }

    fun stop() {
        releaseDisplay()
        projection?.unregisterCallback(callback)
        projection?.stop()
        projection = null
    }

    companion object {
        private const val DISPLAY_NAME = "mirra-capture"
    }
}
