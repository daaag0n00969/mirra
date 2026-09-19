package dev.dag0n.mirra.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _value = MutableStateFlow(read())
    val value: StateFlow<AppSettings> = _value.asStateFlow()

    fun current(): AppSettings = _value.value

    fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(_value.value)
        write(next)
        _value.value = next
    }

    fun reset() {
        prefs.edit().clear().apply()
        _value.value = AppSettings()
    }

    private fun read(): AppSettings = AppSettings(
        flipHorizontal = prefs.getBoolean(FLIP_H, true),
        flipVertical = prefs.getBoolean(FLIP_V, false),
        rotate180 = prefs.getBoolean(ROTATE, false),
        cropSystemBars = prefs.getBoolean(CROP_BARS, false),
        passThroughTouches = prefs.getBoolean(PASS_THROUGH, true),
        remapTouches = prefs.getBoolean(REMAP, false),
        showBubble = prefs.getBoolean(BUBBLE, true),
        keepScreenOn = prefs.getBoolean(KEEP_ON, true),
        overlayOpacityPercent = prefs.getInt(OPACITY, 100).coerceIn(20, 100),
        startDelaySeconds = prefs.getInt(DELAY, 0).coerceIn(0, 15),
        bubbleX = prefs.getInt(BUBBLE_X, Int.MIN_VALUE),
        bubbleY = prefs.getInt(BUBBLE_Y, Int.MIN_VALUE),
    )

    private fun write(s: AppSettings) {
        prefs.edit()
            .putBoolean(FLIP_H, s.flipHorizontal)
            .putBoolean(FLIP_V, s.flipVertical)
            .putBoolean(ROTATE, s.rotate180)
            .putBoolean(CROP_BARS, s.cropSystemBars)
            .putBoolean(PASS_THROUGH, s.passThroughTouches)
            .putBoolean(REMAP, s.remapTouches)
            .putBoolean(BUBBLE, s.showBubble)
            .putBoolean(KEEP_ON, s.keepScreenOn)
            .putInt(OPACITY, s.overlayOpacityPercent)
            .putInt(DELAY, s.startDelaySeconds)
            .putInt(BUBBLE_X, s.bubbleX)
            .putInt(BUBBLE_Y, s.bubbleY)
            .apply()
    }

    companion object {
        private const val PREFS = "mirra_settings"
        private const val FLIP_H = "flip_h"
        private const val FLIP_V = "flip_v"
        private const val ROTATE = "rotate_180"
        private const val CROP_BARS = "crop_bars"
        private const val PASS_THROUGH = "pass_through"
        private const val REMAP = "remap"
        private const val BUBBLE = "bubble"
        private const val KEEP_ON = "keep_on"
        private const val OPACITY = "opacity"
        private const val DELAY = "delay"
        private const val BUBBLE_X = "bubble_x"
        private const val BUBBLE_Y = "bubble_y"
    }
}
