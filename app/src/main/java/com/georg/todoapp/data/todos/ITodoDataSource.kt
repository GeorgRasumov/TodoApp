package com.georg.todoapp.data.todos

import com.georg.todoapp.data.DateType

interface ITodoDataSource {
    fun getTodoItem(id: Int): TodoItem?

    fun addTodoItem(todoItem: IReadOnlyTodoItem)

    fun removeTodoItem(id: Int): Boolean

    fun getTodoItems(dateType: DateType): List<TodoItem>

    fun updateTitle(
        id: Int,
        title: String,
    ): Boolean

    fun updateDateType(
        id: Int,
        dateType: DateType,
    ): Boolean

    fun updateDescription(
        id: Int,
        description: String,
    ): Boolean

    fun updatePosition(
        id: Int,
        position: Int,
    ): Boolean

    fun updateIsCompleted(
        id: Int,
        isCompleted: Boolean,
    ): Boolean
}
