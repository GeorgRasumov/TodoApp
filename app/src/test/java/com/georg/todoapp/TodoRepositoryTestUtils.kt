package com.georg.todoapp

import com.georg.todoapp.data.DateType
import com.georg.todoapp.data.todos.ReadOnlyTodoItemList
import com.georg.todoapp.data.todos.TodoItem
import com.georg.todoapp.data.todos.TodoRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TodoItemTestCollector(
    private val todoItemList: ReadOnlyTodoItemList
) {
    val collectedTitles = mutableListOf<String>()
    val collectedDescriptions = mutableListOf<String>()
    val collectedDateTypes = mutableListOf<DateType>()
    val collectedPositions = mutableListOf<Int>()
    val collectedCompletionStates = mutableListOf<Boolean>()

    val collectedItemsAdded = mutableListOf<Int>()
    val collectedItemsRemoved = mutableListOf<Int>()
    val collectedPositionsChanged = mutableListOf<Boolean>()

    private val jobs = mutableListOf<Job>()

    private val readinessDeferred = CompletableDeferred<Unit>()

    suspend fun startCollecting(scope: CoroutineScope) {
        for (todoItem in todoItemList.items.values) {
            jobs += scope.launch { todoItem.title.collect { collectedTitles.add(it) } }
            jobs += scope.launch { todoItem.description.collect { collectedDescriptions.add(it) } }
            jobs += scope.launch { todoItem.dateType.collect { collectedDateTypes.add(it) } }
            jobs += scope.launch { todoItem.position.collect { collectedPositions.add(it) } }
            jobs += scope.launch { todoItem.isCompleted.collect { collectedCompletionStates.add(it) } }
        }
        jobs += scope.launch { todoItemList.itemAdded.collect { collectedItemsAdded.add(it) } }
        jobs += scope.launch { todoItemList.itemRemoved.collect { collectedItemsRemoved.add(it) } }
        jobs += scope.launch {
            readinessDeferred.complete(Unit)
            todoItemList.positionsChanged.collect {
                collectedPositionsChanged.add(true) } }
        readinessDeferred.await()
    }

    fun stopCollecting() {
        jobs.forEach { it.cancel() }
    }
}


fun addTestTodoItemToRepository(todoRepository: TodoRepository, todoItem: TodoItem) {
    todoRepository.createNewTodoItem(
        todoItem.title.value,
        todoItem.description.value,
        todoItem.dateType.value,
        todoItem.position.value,
        todoItem.isCompleted.value
    )
}