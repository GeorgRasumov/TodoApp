package com.georg.todoapp.data.todos

import com.georg.todoapp.data.todos.IReadOnlyTodoItem
import com.georg.todoapp.utils.IObservableList
import kotlinx.coroutines.flow.SharedFlow

interface ReadOnlyTodoItemList : IObservableList<Int, IReadOnlyTodoItem> {
    val positionsChanged: SharedFlow<Unit>
}
