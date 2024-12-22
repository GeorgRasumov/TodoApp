package com.georg.todoapp.data.todos

import com.georg.todoapp.data.todos.IReadOnlyTodoItem
import com.georg.todoapp.utils.IObservableList

interface ReadOnlyTodoItemList : IObservableList<Int, IReadOnlyTodoItem> {
    fun dispose()
}
