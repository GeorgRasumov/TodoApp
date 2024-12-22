package com.georg.todoapp.utils

class ChangeNotifier<Index, Type> : IChangeNotifier<Index, Type> {
    private val listeners = mutableMapOf<Index, MutableList<Type>>()

    override fun addListener(
        index: Index,
        listener: Type,
    ) {
        if (!listeners.containsKey(index)) {
            listeners[index] = mutableListOf()
        }
        listeners[index]!!.add(listener)
    }

    override fun removeListener(
        index: Index,
        listener: Type,
    ) {
        if (listeners.containsKey(index)) {
            listeners[index]!!.remove(listener)
            if (listeners[index]!!.isEmpty()) {
                listeners.remove(index)
            }
        }
    }

    fun notifyListeners(
        index: Index,
        action: (Type) -> Unit,
    ) {
        if (listeners.containsKey(index)) {
            listeners[index]!!.forEach {
                action(it)
            }
        }
    }
}
