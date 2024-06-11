package net.polix.system.event

import org.springframework.stereotype.Service
import w.eventbus.Event
import w.eventbus.EventBus
import w.eventbus.NamespaceValidator
import w.eventbus.SimpleEventBus

@Service
class EventService {

    val eventBus: EventBus = SimpleEventBus.create(NamespaceValidator.permitAll())

    val events = mutableSetOf<Event>()

    fun registerEvent(event: Event) = events.add(event)

    fun unregisterEvent(event: Event) = events.remove(event)

    fun findEvent(name: String): Event? = events.first {
        it.javaClass.simpleName.lowercase() == name.lowercase()
    }
    fun dispatchEvent(event: Event) = eventBus.dispatch(event)

    fun dispatchEvent(name: String) {
        val event = findEvent(name) ?: return
        dispatchEvent(event)
    }
}