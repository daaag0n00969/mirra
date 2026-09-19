package dev.dag0n.mirra.data

data class AppSettings(
    val flipHorizontal: Boolean = true,
    val flipVertical: Boolean = false,
    val rotate180: Boolean = false,
    val cropSystemBars: Boolean = false,
    val passThroughTouches: Boolean = true,
    val remapTouches: Boolean = false,
    val showBubble: Boolean = true,
    val keepScreenOn: Boolean = true,
    val overlayOpacityPercent: Int = 100,
    val startDelaySeconds: Int = 0,
    val bubbleX: Int = Int.MIN_VALUE,
    val bubbleY: Int = Int.MIN_VALUE,
)
