// credits: ChatGPT. I did not write this piece of code neither I understand a thing here (yet

package com.seniru.walley.persistence

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object LiveDataEventBus {
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun sendEvent(event: String) {
        _events.tryEmit(event)
    }
}
