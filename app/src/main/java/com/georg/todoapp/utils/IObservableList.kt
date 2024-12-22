package com.georg.todoapp.utils

import kotlinx.coroutines.flow.SharedFlow

interface IObservableList<Index, ReadOnlyItem> {
    val items: Map<Index, ReadOnlyItem> // List is not mutable by default

    val itemAdded: SharedFlow<Index>
    val itemRemoved: SharedFlow<Index>
}
