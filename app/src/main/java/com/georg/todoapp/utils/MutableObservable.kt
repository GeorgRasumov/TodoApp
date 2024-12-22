package com.georg.todoapp.utils

import java.lang.ref.WeakReference

class MutableObservable<T>(
    private var initialValue: T,
) : IObservable<T> {
    // Use WeakReference to hold listeners
    private val listeners = mutableListOf<WeakReference<(T, T) -> Unit>>()

    override var value: T = initialValue

        set(newValue: T) {
            val oldValue = field
            field = newValue

            // Notify listeners, clean up garbage-collected ones
            val iterator = listeners.iterator()
            while (iterator.hasNext()) {
                val listenerRef = iterator.next()
                val listener = listenerRef.get()
                if (listener != null) {
                    listener(oldValue, newValue)
                } else {
                    // Remove listener if it has been garbage collected
                    iterator.remove()
                }
            }
        }

    override fun addListener(listener: (T, T) -> Unit) {
        listeners.add(WeakReference(listener))
    }

    override fun removeListener(listener: (T, T) -> Unit) {
        // Remove listener manually by checking references
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
