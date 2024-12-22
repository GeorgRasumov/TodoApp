package com.georg.todoapp.utils

import java.lang.ref.WeakReference

class MutableEvent<T> : IEvent<T> {
    // Use WeakReference to hold listeners
    private val listeners = mutableListOf<WeakReference<(T) -> Unit>>()

    /**
     * Triggers the event with the provided value.
     * Notifies all listeners and removes garbage-collected ones.
     */
    fun trigger(value: T) {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            val listenerRef = iterator.next()
            val listener = listenerRef.get()
            if (listener != null) {
                listener(value)
            } else {
                // Remove listener if it has been garbage collected
                iterator.remove()
            }
        }
    }

    /**
     * Adds a listener that will be called whenever the event is triggered.
     */
    override fun addListener(listener: (T) -> Unit) {
        listeners.add(WeakReference(listener))
    }

    /**
     * Removes a listener from the list.
     */
    override fun removeListener(listener: (T) -> Unit) {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            val listenerRef = iterator.next()
            val existingListener = listenerRef.get()
            if (existingListener == listener) {
                iterator.remove()
            }
        }
    }
}
