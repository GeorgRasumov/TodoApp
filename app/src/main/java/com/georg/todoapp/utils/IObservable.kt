package com.georg.todoapp.utils

interface IObservable<T> {
    val value: T

    fun addListener(listener: (T, T) -> Unit)

    fun removeListener(listener: (T, T) -> Unit)
}
