package com.georg.todoapp.data.todos

import com.georg.todoapp.data.DateType
import com.georg.todoapp.data.IUniqueIdProvider
import com.georg.todoapp.utils.ChangeNotifier
import com.georg.todoapp.utils.IEvent
import com.georg.todoapp.utils.MutableEvent

class TodoRepository(
    private val uniqueIdProvider: IUniqueIdProvider,
    private val dataSource: ITodoDataSource,
) {
    private val _updateTodosEvent = MutableEvent<DateType>()
    val updateTodosEvent: IEvent<DateType> = _updateTodosEvent
    private val _todoAddedEvent = MutableEvent<IReadOnlyTodoItem>()
    val todoAddedEvent: IEvent<IReadOnlyTodoItem> = _todoAddedEvent

    // Do not directly assign a todoItem but use the add Method from the item instance
    private val todoChangedNotifier = ChangeNotifier<Int, TodoItem>()

    // Do not directly assign a list but use the add Method from the list instance
    private val todoListChangedNotifier = ChangeNotifier<DateType, TodoItemList>()

    fun createNewTodoItem(
        title: String,
        description: String,
        dateType: DateType,
        position: Int,
        isCompleted: Boolean,
    ): TodoItem {
        val newTodoItem =
            TodoItem(
                id = uniqueIdProvider.getUniqueId(),
                title = title,
                description = description,
                dateType = dateType,
                position = position,
                isCompleted = isCompleted,
            )
        newTodoItem.setChangeNotifier(todoChangedNotifier)
        todoListChangedNotifier.notifyListeners(dateType) {
            // Copy the item so that one receiver can dispose the item without affecting the other receivers
            val copiedTodoItem = newTodoItem.copy()
            copiedTodoItem.setChangeNotifier(todoChangedNotifier)
            it.add(newTodoItem.id, copiedTodoItem)
        }
        dataSource.addTodoItem(newTodoItem)
        return newTodoItem
    }

    fun getTodoItems(dateType: DateType): ReadOnlyTodoItemList {
        val todoList = dataSource.getTodoItems(dateType)
        todoList.items.forEach { it.value.setChangeNotifier(todoChangedNotifier) }
        todoList.setChangeNotifier(todoListChangedNotifier)
        return todoList
    }

    fun editTitle(
        id: Int,
        title: String,
    ) {
        todoChangedNotifier.notifyListeners(id) { it.title.value = title }
        dataSource.updateTitle(id, title)
    }

    fun editDescription(
        id: Int,
        description: String,
    ) {
        todoChangedNotifier.notifyListeners(id) { it.description.value = description }
        dataSource.updateDescription(id, description)
    }

    fun editDateType(
        id: Int,
        newDateType: DateType,
    ) {
        todoChangedNotifier.notifyListeners(id) { it.dateType.value = newDateType }
        dataSource.updateDateType(id, newDateType)
    }

    fun editPosition(
        id: Int,
        position: Int,
    ) {
        todoChangedNotifier.notifyListeners(id) { it.position.value = position }
        dataSource.updatePosition(id, position)
    }

    fun editIsCompleted(
        id: Int,
        isCompleted: Boolean,
    ) {
        todoChangedNotifier.notifyListeners(id) { it.isCompleted.value = isCompleted }
        dataSource.updateIsCompleted(id, isCompleted)
    }
}
