package com.georg.todoapp.data.todos

import com.georg.todoapp.data.DateType
import com.georg.todoapp.utils.MutableObservableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class TodoItemList(
    val dateType: DateType,
) : MutableObservableList<Int, TodoItem, IReadOnlyTodoItem>(),
    ReadOnlyTodoItemList {
    private val _positionsChanged = MutableSharedFlow<Unit>(extraBufferCapacity = Int.MAX_VALUE)
    override val positionsChanged = _positionsChanged.asSharedFlow()

    override fun hasObservers(): Boolean = super.hasObservers() || _positionsChanged.subscriptionCount.value > 0

    fun positionsChanged() {
        _positionsChanged.tryEmit(Unit)
    }
}
