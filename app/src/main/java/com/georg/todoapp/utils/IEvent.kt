package com.georg.todoapp.utils

interface IEvent<T> {
    /**
     * Adds a listener to the event.
     */
    fun addListener(listener: (T) -> Unit)

    /**
     * Removes a listener from the event.
     */
    fun removeListener(listener: (T) -> Unit)
}
