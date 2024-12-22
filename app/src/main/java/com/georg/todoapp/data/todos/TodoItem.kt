package com.georg.todoapp.data.todos

import com.georg.todoapp.data.DateType
import com.georg.todoapp.utils.IChangeNotifier
import kotlinx.coroutines.flow.MutableStateFlow

class TodoItem(
    val id: Int,
    title: String,
    dateType: DateType,
    description: String = "",
    position: Int = 0,
    isCompleted: Boolean = false,
) : IReadOnlyTodoItem {
    private var changeNotifier: IChangeNotifier<Int, TodoItem>? = null

    override val title = MutableStateFlow<String>(title)
    override val dateType = MutableStateFlow<DateType>(dateType)
    override val description = MutableStateFlow<String>(description)
    override val position = MutableStateFlow<Int>(position)
    override val isCompleted = MutableStateFlow<Boolean>(isCompleted)

    fun setChangeNotifier(newChangeNotifier: IChangeNotifier<Int, TodoItem>) {
        changeNotifier?.removeListener(id, this)
        changeNotifier = newChangeNotifier
        changeNotifier!!.addListener(id, this)
    }

    fun dispose() {
        changeNotifier?.removeListener(id, this)
    }

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
