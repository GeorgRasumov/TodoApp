package com.georg.todoapp.data.todos

import com.georg.todoapp.data.DateType
import com.georg.todoapp.data.IUniqueIdProvider
import com.georg.todoapp.utils.IEvent
import com.georg.todoapp.utils.MutableEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TodoRepository(
    private val uniqueIdProvider: IUniqueIdProvider,
    private val dataSource: ITodoDataSource,
    private val scope: CoroutineScope,
) : ITodoRepository {
    private val _onGetTodosEvent = MutableEvent<DateType>()
    val onGetTodosEvent: IEvent<DateType> = _onGetTodosEvent

    private val todoItems = mutableMapOf<Int, TodoItem>()
    private val todoItemLists = mutableMapOf<DateType, TodoItemList>()

    init {
        // Start the coroutine as soon as the instance is created
        scope.launch {
            while (isActive) {
                delay(10_000L) // Wait for 10 seconds
                cleanupLists()
            }
        }
    }

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
        todoItems[newTodoItem.id] = newTodoItem
        todoItemLists[dateType]?.add(newTodoItem.id, newTodoItem)
        dataSource.addTodoItem(newTodoItem)
        return newTodoItem
    }

    fun deleteTodoItem(id: Int) {
        val todoItem = getTodoItem(id) ?: throw IllegalArgumentException("Todo item with id $id not found")
        todoItems.remove(id)
        todoItemLists[todoItem.dateType.value]?.remove(id)
        dataSource.removeTodoItem(id)
    }

    fun getTodoItems(dateType: DateType): TodoItemList {
        val todoList =
            if (todoItemLists.containsKey(dateType)) {
                todoItemLists[dateType]!!
            } else {
                val newItems = dataSource.getTodoItems(dateType)
                todoItemLists[dateType] = newItems
                for (item in newItems.items.values) {
                    todoItems[item.id] = item
                }
                newItems
            }
        _onGetTodosEvent.trigger(dateType) // Update after the items have been loaded, so there is a list the items can be added to
        return todoList
    }

    fun getTodoItem(id: Int): TodoItem? = todoItems[id] ?: dataSource.getTodoItem(id)

    fun editTitle(
        id: Int,
        title: String,
    ) {
        todoItems[id]?.title?.value = title
        dataSource.updateTitle(id, title)
    }

    fun editDescription(
        id: Int,
        description: String,
    ) {
        todoItems[id]?.description?.value = description
        dataSource.updateDescription(id, description)
    }

    fun editDateType(
        id: Int,
        newDateType: DateType,
    ) {
        val todoItem = getTodoItem(id) ?: throw IllegalArgumentException("Todo item with id $id not found")
        todoItemLists[todoItem.dateType.value]?.remove(id)
        todoItem.dateType.value = newDateType
        todoItemLists[newDateType]?.add(id, todoItem)
        dataSource.updateDateType(id, newDateType)
    }

    private fun editPosition(
        id: Int,
        position: Int,
    ) {
        todoItems[id]?.position?.value = position
        dataSource.updatePosition(id, position)
    }

    fun updatePosition(
        id: Int,
        position: Int,
    ) {
        val todoItem = getTodoItem(id) ?: throw IllegalArgumentException("Todo item with id $id not found")
        val todoItems = getTodoItems(todoItem.dateType.value).items.values.toMutableList()
        val oldPosition = todoItem.position.value

        val sortedItems = todoItems.sortedBy { it.position.value }.toMutableList()
        sortedItems.removeAt(oldPosition)
        sortedItems.add(position, todoItem)

        sortedItems.forEachIndexed { index, item ->
            editPosition(item.id, index)
        }
        todoItemLists[todoItem.dateType.value]?.positionsChanged()
    }

    fun editIsCompleted(
        id: Int,
        isCompleted: Boolean,
    ) {
        todoItems[id]?.isCompleted?.value = isCompleted
        dataSource.updateIsCompleted(id, isCompleted)
    }

    private var itemsMarkedForRemoval = mutableSetOf<Int>()
    private var listsMarkedForRemoval = mutableSetOf<DateType>()

    private fun cleanupLists() {
        // Determine lists with no observers
        val currentListsToRemove =
            todoItemLists
                .filterValues { itemList ->
                    !itemList.hasObservers()
                }.keys

        // Determine items with no observers
        val currentItemsToRemove =
            todoItems
                .filterValues { item ->
                    !item.hasObservers()
                }.keys

        // Remove lists that were marked in the previous run and are still marked
        val confirmedListsToRemove = listsMarkedForRemoval.intersect(currentListsToRemove)
        for (key in confirmedListsToRemove) {
            todoItemLists.remove(key)
        }

        // Remove items that were marked in the previous run and are still marked
        val confirmedItemsToRemove = itemsMarkedForRemoval.intersect(currentItemsToRemove)
        for (key in confirmedItemsToRemove) {
            todoItems.remove(key)
        }

        // Update the marked sets for the next run
        listsMarkedForRemoval = currentListsToRemove.toMutableSet()
        itemsMarkedForRemoval = currentItemsToRemove.toMutableSet()

        println("Cleaned up ${confirmedItemsToRemove.size} empty items at ${System.currentTimeMillis()}")
        println("Cleaned up ${confirmedListsToRemove.size} empty lists at ${System.currentTimeMillis()}")
    }
}
