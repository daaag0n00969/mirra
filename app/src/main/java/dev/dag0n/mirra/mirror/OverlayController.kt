package dev.dag0n.mirra.mirror

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import dev.dag0n.mirra.a11y.MirraAccessibilityService
import dev.dag0n.mirra.data.AppSettings
import dev.dag0n.mirra.data.SettingsStore

class OverlayController(
    private val serviceContext: Context,
    private val settingsStore: SettingsStore,
    private val capture: CaptureEngine,
    private val cropTop: Int,
    private val cropBottom: Int,
    private val cropLeft: Int,
    private val cropRight: Int,
) {
    private var root: FrameLayout? = null
    private var textureView: TextureView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var visible = false
    private var remapListener: TouchMapper? = null

    private val windowManager: WindowManager
        get() = hostContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val hostContext: Context
        get() = MirraAccessibilityService.instance ?: serviceContext

    fun attach() {
        if (root != null) return
        val tv = TextureView(hostContext).apply {
            isOpaque = true
            setBackgroundColor(Color.BLACK)
            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                    capture.bindSurface(Surface(st), width.coerceAtLeast(1), height.coerceAtLeast(1))
                    applyVisualTransform()
                }

                override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {
                    capture.resize(width.coerceAtLeast(1), height.coerceAtLeast(1))
                    applyVisualTransform()
                }

                override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                    capture.releaseDisplay()
                    return true
                }

                override fun onSurfaceTextureUpdated(st: SurfaceTexture) = Unit
            }
        }
        textureView = tv
        val frame = FrameLayout(hostContext).apply {
            // Solid black so the real (unflipped) app never shows through
            // TextureView holes, transforms, or window blending.
            setBackgroundColor(Color.BLACK)
            addView(
                tv,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                ),
            )
        }
        root = frame
        val params = buildParams(settingsStore.current())
        layoutParams = params
        windowManager.addView(frame, params)
        visible = true
        applySettings(settingsStore.current())
        MirrorState.setOverlayVisible(true)
    }

    fun detach() {
        root?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        root = null
        textureView = null
        layoutParams = null
        visible = false
        remapListener = null
        MirrorState.setOverlayVisible(false)
    }

    fun isVisible(): Boolean = visible && root?.visibility == View.VISIBLE

    fun setVisible(show: Boolean) {
        val view = root ?: return
        view.visibility = if (show) View.VISIBLE else View.GONE
        visible = show
        MirrorState.setOverlayVisible(show)
    }

    fun toggle(): Boolean {
        val next = !isVisible()
        setVisible(next)
        return next
    }

    fun applySettings(settings: AppSettings) {
        applyVisualTransform()
        val params = layoutParams ?: return
        val view = root ?: return
        val rebuilt = buildParams(settings)
        params.flags = rebuilt.flags
        params.alpha = rebuilt.alpha
        runCatching { windowManager.updateViewLayout(view, params) }
        bindTouch(settings)
    }

    fun setPassThroughTemporarily(passThrough: Boolean) {
        val view = root ?: return
        val params = layoutParams ?: return
        if (passThrough) {
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        } else {
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        }
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun bindTouch(settings: AppSettings) {
        val view = root ?: return
        if (settings.remapTouches && MirraAccessibilityService.isEnabled()) {
            val mapper = TouchMapper(
                settings = { settingsStore.current() },
                setPassThrough = { setPassThroughTemporarily(it) },
            )
            remapListener = mapper
            view.setOnTouchListener(mapper)
        } else {
            remapListener = null
            view.setOnTouchListener(null)
        }
    }

    private fun applyVisualTransform() {
        val tv = textureView ?: return
        val settings = settingsStore.current()
        tv.scaleX = if (settings.flipHorizontal) -1f else 1f
        tv.scaleY = if (settings.flipVertical) -1f else 1f
        tv.rotation = if (settings.rotate180) 180f else 0f
        // Fade the picture toward black, never toward the unflipped app underneath.
        tv.alpha = (settings.overlayOpacityPercent / 100f).coerceIn(0.2f, 1f)
        if (settings.cropSystemBars && tv.width > 0 && tv.height > 0) {
            val w = tv.width.toFloat()
            val h = tv.height.toFloat()
            val cropW = (w - cropLeft - cropRight).coerceAtLeast(1f)
            val cropH = (h - cropTop - cropBottom).coerceAtLeast(1f)
            val scale = maxOf(w / cropW, h / cropH)
            tv.pivotX = w / 2f
            tv.pivotY = h / 2f
            tv.scaleX *= scale
            tv.scaleY *= scale
        }
    }

    private fun buildParams(settings: AppSettings): WindowManager.LayoutParams {
        val a11y = MirraAccessibilityService.isEnabled()
        val type = if (a11y) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        val remap = settings.remapTouches && a11y
        val passThrough = settings.passThroughTouches && !remap
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        if (passThrough) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
        if (settings.keepScreenOn) {
            flags = flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        }
        // Window alpha stays at 1 so the compositor does not blend in the
        // unflipped app (that was the "double image" bug). User opacity is
        // applied to the TextureView over a black backdrop instead.
        //
        // FLAG_NOT_TOUCHABLE + alpha 1.0 is allowed for TYPE_ACCESSIBILITY_OVERLAY
        // (trusted). For a normal overlay Android 12+ may drop pass-through taps;
        // the floating bubble still works, and enabling Accessibility restores
        // full tap-through on an opaque picture.
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.alpha = 1f
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
    }
}
