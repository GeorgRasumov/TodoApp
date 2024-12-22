package com.georg.todoapp.data.todos

import com.georg.todoapp.data.DateType

interface ITodoDataSource {
    fun getTodoItem(id: Int): TodoItem

    fun addTodoItem(todoItem: IReadOnlyTodoItem)

    fun removeTodoItem(id: Int)

    fun getTodoItems(dateType: DateType): TodoItemList

    fun updateTitle(
        id: Int,
        title: String,
    )

    fun updateDateType(
        id: Int,
        dateType: DateType,
    )

    fun updateDescription(
        id: Int,
        description: String,
    )

    fun updatePosition(
        id: Int,
        position: Int,
    )

    fun updateIsCompleted(
        id: Int,
        isCompleted: Boolean,
    )
}
