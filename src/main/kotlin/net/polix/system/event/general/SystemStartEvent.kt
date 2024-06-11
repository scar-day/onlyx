package net.polix.system.event.general

import w.eventbus.Event

data class SystemStartEvent(
    val time: Long
) : Event
