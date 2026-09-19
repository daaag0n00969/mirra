package dev.dag0n.mirra.mirror

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import dev.dag0n.mirra.R
import dev.dag0n.mirra.a11y.MirraAccessibilityService
import dev.dag0n.mirra.data.SettingsStore
import kotlin.math.abs
import kotlin.math.hypot

class BubbleController(
    private val serviceContext: Context,
    private val settingsStore: SettingsStore,
    private val onToggleOverlay: () -> Unit,
    private val onStop: () -> Unit,
    private val onFlipHorizontal: () -> Unit,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var bubble: FrameLayout? = null
    private var panel: LinearLayout? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var icon: ImageView? = null
    private var overlayOn = true

    private val windowManager: WindowManager
        get() = hostContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val hostContext: Context
        get() = MirraAccessibilityService.instance ?: serviceContext

    fun attach() {
        if (bubble != null) return
        val density = serviceContext.resources.displayMetrics.density
        val size = (56 * density).toInt()
        val view = FrameLayout(hostContext)
        val image = ImageView(hostContext).apply {
            setImageResource(R.drawable.ic_bubble_on)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            background = circle("#14222BCC".toColorInt(), (28 * density))
            setPadding((12 * density).toInt(), (12 * density).toInt(), (12 * density).toInt(), (12 * density).toInt())
            contentDescription = serviceContext.getString(R.string.bubble_toggle)
        }
        icon = image
        view.addView(image, FrameLayout.LayoutParams(size, size))
        val metrics = serviceContext.resources.displayMetrics
        val settings = settingsStore.current()
        val startX = if (settings.bubbleX != Int.MIN_VALUE) settings.bubbleX else metrics.widthPixels - size - (16 * density).toInt()
        val startY = if (settings.bubbleY != Int.MIN_VALUE) settings.bubbleY else (metrics.heightPixels * 0.38f).toInt()
        val params = WindowManager.LayoutParams(
            size,
            size,
            windowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = startX.coerceAtLeast(0)
            y = startY.coerceAtLeast(0)
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
        installDrag(view, params, size)
        bubble = view
        bubbleParams = params
        windowManager.addView(view, params)
    }

    fun detach() {
        hidePanel()
        bubble?.let { runCatching { windowManager.removeView(it) } }
        bubble = null
        bubbleParams = null
        icon = null
    }

    fun setOverlayActive(active: Boolean) {
        overlayOn = active
        icon?.setImageResource(if (active) R.drawable.ic_bubble_on else R.drawable.ic_bubble_off)
        icon?.background = circle(
            if (active) "#14222BCC".toColorInt() else "#3A2414CC".toColorInt(),
            28f * serviceContext.resources.displayMetrics.density,
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun installDrag(view: FrameLayout, params: WindowManager.LayoutParams, size: Int) {
        val slop = ViewConfiguration.get(serviceContext).scaledTouchSlop
        val longPress = ViewConfiguration.getLongPressTimeout().toLong()
        var downX = 0f
        var downY = 0f
        var originX = 0
        var originY = 0
        var dragged = false
        var longFired = false
        val longRunnable = Runnable {
            if (!dragged) {
                longFired = true
                showPanel()
            }
        }
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    originX = params.x
                    originY = params.y
                    dragged = false
                    longFired = false
                    handler.postDelayed(longRunnable, longPress)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (!dragged && hypot(dx, dy) > slop) {
                        dragged = true
                        handler.removeCallbacks(longRunnable)
                        hidePanel()
                    }
                    if (dragged) {
                        val metrics = serviceContext.resources.displayMetrics
                        params.x = (originX + dx.toInt()).coerceIn(0, metrics.widthPixels - size)
                        params.y = (originY + dy.toInt()).coerceIn(0, metrics.heightPixels - size)
                        runCatching { windowManager.updateViewLayout(view, params) }
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(longRunnable)
                    if (dragged) {
                        snap(params, size)
                        settingsStore.update { it.copy(bubbleX = params.x, bubbleY = params.y) }
                    } else if (!longFired && event.actionMasked == MotionEvent.ACTION_UP) {
                        if (panel != null) hidePanel() else onToggleOverlay()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun snap(params: WindowManager.LayoutParams, size: Int) {
        val view = bubble ?: return
        val metrics = serviceContext.resources.displayMetrics
        val mid = metrics.widthPixels / 2
        params.x = if (params.x + size / 2 < mid) (8 * metrics.density).toInt()
        else metrics.widthPixels - size - (8 * metrics.density).toInt()
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun showPanel() {
        if (panel != null) return
        val density = serviceContext.resources.displayMetrics.density
        val bubbleP = bubbleParams ?: return
        val layout = LinearLayout(hostContext).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded("#E0121A2B".toColorInt(), 16 * density)
            setPadding((12 * density).toInt(), (10 * density).toInt(), (12 * density).toInt(), (10 * density).toInt())
            elevation = 12 * density
        }
        layout.addView(panelButton(serviceContext.getString(
            if (overlayOn) R.string.action_hide_overlay else R.string.action_show_overlay
        )) { onToggleOverlay(); hidePanel() })
        layout.addView(panelButton(serviceContext.getString(R.string.setting_flip_h)) {
            onFlipHorizontal(); hidePanel()
        })
        layout.addView(panelButton(serviceContext.getString(R.string.action_stop)) {
            hidePanel(); onStop()
        })
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bubbleP.x
            y = bubbleP.y + (60 * density).toInt()
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
        panel = layout
        windowManager.addView(layout, params)
        handler.postDelayed({ hidePanel() }, 5000)
    }

    private fun hidePanel() {
        handler.removeCallbacksAndMessages(null)
        panel?.let { runCatching { windowManager.removeView(it) } }
        panel = null
    }

    private fun panelButton(label: String, onClick: () -> Unit): TextView {
        val density = serviceContext.resources.displayMetrics.density
        return TextView(hostContext).apply {
            text = label
            setTextColor("#E6FFFFFF".toColorInt())
            textSize = 14f
            setPadding((8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt(), (8 * density).toInt())
            setOnClickListener { onClick() }
        }
    }

    private fun windowType(): Int {
        return if (MirraAccessibilityService.isEnabled()) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
    }

    private fun circle(color: Int, radius: Float) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        cornerRadius = radius
        setStroke((2 * serviceContext.resources.displayMetrics.density).toInt(), "#2EE6D6".toColorInt())
    }

    private fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = radius
        setStroke((1 * serviceContext.resources.displayMetrics.density).toInt(), "#662EE6D6".toColorInt())
    }
}
