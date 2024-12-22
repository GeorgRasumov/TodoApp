package com.georg.todoapp.utils

interface IChangeNotifier<Index, Type> {
    fun addListener(
        index: Index,
        listener: Type,
    )

    fun removeListener(
        index: Index,
        listener: Type,
    )
}
