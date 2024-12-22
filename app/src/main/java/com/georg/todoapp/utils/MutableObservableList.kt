package com.georg.todoapp.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Index is the type of the index used to access the items in the list
open class MutableObservableList<Index, MutableItem, ReadOnlyItem> : IObservableList<Index, ReadOnlyItem> where MutableItem : ReadOnlyItem {
    private val _items = mutableMapOf<Index, MutableItem>()
    override val items = _items
    private val _itemAdded = MutableSharedFlow<Index>(extraBufferCapacity = Int.MAX_VALUE) // replay = Int.MAX_VALUE)
    override val itemAdded = _itemAdded.asSharedFlow()
    private val _itemRemoved = MutableSharedFlow<Index>(extraBufferCapacity = Int.MAX_VALUE) // replay = Int.MAX_VALUE)
    override val itemRemoved = _itemRemoved.asSharedFlow()

    fun add(
        index: Index,
        item: MutableItem,
    ) {
        if (_items.putIfAbsent(index, item) != null) {
            throw IllegalArgumentException("An item with the given index already exists: $index")
        }
        _itemAdded.tryEmit(index)
    }

    fun remove(index: Index) {
        _items.remove(index) ?: throw IllegalArgumentException("No item found for the given index: $index")
        _itemRemoved.tryEmit(index)
    }

    fun get(index: Index): MutableItem = items[index] ?: throw IllegalArgumentException("No item found for the given index: $index")

    open fun hasObservers(): Boolean = _itemAdded.subscriptionCount.value > 0 || _itemRemoved.subscriptionCount.value > 0
}
