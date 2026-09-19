package dev.dag0n.mirra.mirror

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object MirrorState {
    private val _running = MutableStateFlow(false)
    val running = _running.asStateFlow()

    private val _overlayVisible = MutableStateFlow(false)
    val overlayVisible = _overlayVisible.asStateFlow()

    private val _status = MutableStateFlow("")
    val status = _status.asStateFlow()

    fun setRunning(value: Boolean) {
        _running.value = value
        if (!value) {
            _overlayVisible.value = false
        }
    }

    fun setOverlayVisible(value: Boolean) {
        _overlayVisible.value = value
    }

    fun setStatus(value: String) {
        _status.value = value
    }
}
