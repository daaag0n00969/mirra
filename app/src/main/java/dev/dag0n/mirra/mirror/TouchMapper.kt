package dev.dag0n.mirra.mirror

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import dev.dag0n.mirra.a11y.MirraAccessibilityService
import dev.dag0n.mirra.data.AppSettings
import kotlin.math.abs

/**
 * Maps taps on the flipped overlay back onto the real app underneath.
 * Inverse of the TextureView scale/rotation so a control you see is the
 * control that actually receives the gesture.
 */
class TouchMapper(
    private val settings: () -> AppSettings,
    private val setPassThrough: (Boolean) -> Unit,
) : View.OnTouchListener {

    private val path = Path()
    private var downAt = 0L
    private var tracking = false
    private var lastX = 0f
    private var lastY = 0f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val mapped = mapPoint(event.rawX, event.rawY, v.width.toFloat(), v.height.toFloat(), settings())
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                path.reset()
                path.moveTo(mapped.first, mapped.second)
                lastX = mapped.first
                lastY = mapped.second
                downAt = SystemClock.elapsedRealtime()
                tracking = true
            }
            MotionEvent.ACTION_MOVE -> if (tracking) {
                if (abs(mapped.first - lastX) + abs(mapped.second - lastY) > 1f) {
                    path.lineTo(mapped.first, mapped.second)
                    lastX = mapped.first
                    lastY = mapped.second
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (tracking) {
                path.lineTo(mapped.first, mapped.second)
                val duration = (SystemClock.elapsedRealtime() - downAt).coerceIn(1L, 2500L)
                val stroke = GestureDescription.StrokeDescription(Path(path), 0, duration)
                val gesture = GestureDescription.Builder().addStroke(stroke).build()
                val a11y = MirraAccessibilityService.instance
                if (a11y != null) {
                    setPassThrough(true)
                    val accepted = a11y.inject(gesture) {
                        v.postDelayed({ setPassThrough(false) }, 40)
                    }
                    if (!accepted) setPassThrough(false)
                }
                tracking = false
            }
        }
        return true
    }

    companion object {
        fun mapPoint(
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            settings: AppSettings,
        ): Pair<Float, Float> {
            var mx = x
            var my = y
            if (settings.flipHorizontal) mx = width - x
            if (settings.flipVertical) my = height - y
            if (settings.rotate180) {
                mx = width - mx
                my = height - my
            }
            return mx.coerceIn(0f, width) to my.coerceIn(0f, height)
        }
    }
}
