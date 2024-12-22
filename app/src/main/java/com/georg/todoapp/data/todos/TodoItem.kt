package com.georg.todoapp.data.todos

import com.georg.todoapp.data.DateType
import kotlinx.coroutines.flow.MutableStateFlow

class TodoItem(
    val id: Int,
    title: String,
    dateType: DateType,
    description: String = "",
    position: Int = 0,
    isCompleted: Boolean = false,
) : IReadOnlyTodoItem {
    override val title = MutableStateFlow(title)
    override val dateType = MutableStateFlow(dateType)
    override val description = MutableStateFlow(description)
    override val position = MutableStateFlow(position)
    override val isCompleted = MutableStateFlow(isCompleted)

    fun hasObservers(): Boolean =
        title.subscriptionCount.value > 0 ||
            dateType.subscriptionCount.value > 0 ||
            description.subscriptionCount.value > 0 ||
            position.subscriptionCount.value > 0 ||
            isCompleted.subscriptionCount.value > 0

    fun copy(
        id: Int = this.id,
        title: String = this.title.value,
        dateType: DateType = this.dateType.value,
        description: String = this.description.value,
        position: Int = this.position.value,
        isCompleted: Boolean = this.isCompleted.value,
    ): TodoItem =
        TodoItem(
            id = id,
            title = title,
            dateType = dateType,
            description = description,
            position = position,
            isCompleted = isCompleted,
        )
}
