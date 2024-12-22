package com.georg.todoapp

import com.georg.todoapp.data.DateType
import com.georg.todoapp.data.todos.ITodoDataSource
import com.georg.todoapp.data.IUniqueIdProvider
import com.georg.todoapp.data.todos.TodoItem
import com.georg.todoapp.data.todos.TodoItemList
import com.georg.todoapp.data.todos.TodoRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TodoRepositoryTest {

    @Mock
    private lateinit var uniqueIdProvider: IUniqueIdProvider
    private lateinit var todoRepository: TodoRepository
    private lateinit var todoDataSource: ITodoDataSource

    @Before
    fun setUp() {
        todoDataSource = mock(ITodoDataSource::class.java)

        uniqueIdProvider = mock(IUniqueIdProvider::class.java)
        var currentId = 0
        `when`(uniqueIdProvider.getUniqueId()).thenAnswer {
            currentId++
        }

        todoRepository = TodoRepository(uniqueIdProvider, todoDataSource)
    }

    @Test
    fun `test TodoItem State flow`() = runTest {
        val firstTitle = "firstTitle"
        val secondTitle = "secondTitle"
        val firstDescription = "firstDescription"
        val secondDescription = "secondDescription"
        val firstDateType = DateType.getInstance(DateType.Type.NO_DATE)
        val secondDateType = DateType.getInstance(DateType.Type.FIXED_DATE, LocalDate.now())
        val firstPosition = 0
        val secondPosition = 1
        val firstIsCompleted = false
        val secondIsCompleted = true

        val readOnlyTodoItem = todoRepository.createNewTodoItem(firstTitle, firstDescription, firstDateType, firstPosition, firstIsCompleted)

        // Test 1: Initial StateFlow values
        assertEquals(firstTitle, readOnlyTodoItem.title.value)
        assertEquals(firstDescription, readOnlyTodoItem.description.value)
        assertEquals(firstDateType, readOnlyTodoItem.dateType.value)
        assertEquals(firstPosition, readOnlyTodoItem.position.value)
        assertEquals(firstIsCompleted, readOnlyTodoItem.isCompleted.value)

        // Collect emissions to verify updates
        val collectedTitles = mutableListOf<String>()
        val collectedDescriptions = mutableListOf<String>()
        val collectedDateTypes = mutableListOf<DateType>()
        val collectedPositions = mutableListOf<Int>()
        val collectedCompletionStates = mutableListOf<Boolean>()

        val collectJobTitle = launch {
            readOnlyTodoItem.title.collect { collectedTitles.add(it) }
        }
        val collectJobDescription = launch {
            readOnlyTodoItem.description.collect { collectedDescriptions.add(it) }
        }
        val collectJobDateType = launch {
            readOnlyTodoItem.dateType.collect { collectedDateTypes.add(it) }
        }
        val collectJobPosition = launch {
            readOnlyTodoItem.position.collect { collectedPositions.add(it) }
        }
        val collectJobIsCompleted = launch {
            readOnlyTodoItem.isCompleted.collect { collectedCompletionStates.add(it) }
        }

        // Update values
        todoRepository.editTitle(readOnlyTodoItem.id, secondTitle)
        todoRepository.editDescription(readOnlyTodoItem.id, secondDescription)
        todoRepository.editDateType(readOnlyTodoItem.id, secondDateType)
        todoRepository.editPosition(readOnlyTodoItem.id, secondPosition)
        todoRepository.editIsCompleted(readOnlyTodoItem.id, secondIsCompleted)

        advanceUntilIdle()

        // Verify collected values include the updated state
        assertTrue(collectedTitles.contains(secondTitle))
        assertTrue(collectedDescriptions.contains(secondDescription))
        assertTrue(collectedDateTypes.contains(secondDateType))
        assertTrue(collectedPositions.contains(secondPosition))
        assertTrue(collectedCompletionStates.contains(secondIsCompleted))

        // Cancel jobs to clean up
        collectJobTitle.cancel()
        collectJobDescription.cancel()
        collectJobDateType.cancel()
        collectJobPosition.cancel()
        collectJobIsCompleted.cancel()
    }

    @Test
    fun `TodoItem Dispose`() = runTest {

        val firstTitle = "firstTitle"
        val secondTitle = "secondTitle"
        val readOnlyTodoItem = todoRepository.createNewTodoItem(firstTitle, "Description", DateType.getInstance(DateType.Type.NO_DATE),0, false)

        val collectJob = launch {
            readOnlyTodoItem.title.collect { _ ->
                assertNotEquals(secondTitle, readOnlyTodoItem.title.value)
            }
        }
        readOnlyTodoItem.dispose()
        todoRepository.editTitle(readOnlyTodoItem.id, secondTitle)
        advanceUntilIdle()
        collectJob.cancel()

    }


    @Test
    fun `test receive TodoItemList`() = runTest {
        val noDateType = DateType.getInstance(DateType.Type.NO_DATE)
        val todoList = TodoItemList(noDateType)

        // Setup initial TodoItem
        val initialTitle = "Title1"
        val initialId = uniqueIdProvider.getUniqueId()
        val newTitle = "Title2"
        val newId = uniqueIdProvider.getUniqueId()

        todoList.add(
            initialId, TodoItem(initialId, initialTitle, noDateType, "Description", 0, false)
        )

        // Mock data source
        doReturn(todoList).`when`(todoDataSource).getTodoItems(noDateType)

        // Verify initial state
        val todos = todoRepository.getTodoItems(noDateType)
        assertEquals(1, todos.items.size)
        assertEquals(initialTitle, todos.items[initialId]?.title?.value)

        val startedSignal = CompletableDeferred<Unit>() // Deferred for synchronization
        var coroutineStarted = false
        // Collect added item titles
        val addedTitles = mutableListOf<String>()
        val addedItemsJob = launch {
            coroutineStarted = true
            startedSignal.complete(Unit)
            todos.itemAdded.collect  { id ->
                todos.items[id]?.title?.value?.let { addedTitles.add(it) }
            }
        }

        startedSignal.await()
        assertTrue(coroutineStarted)
        // Add new TodoItem
        todoRepository.createNewTodoItem(newTitle, "Description", noDateType, 0, false)
        advanceUntilIdle()
        assertEquals(2, todos.items.size)
        assertTrue(addedTitles.contains(newTitle))
        addedItemsJob.cancel()

        // Observe updates to existing TodoItem
        val updatedTitles = mutableListOf<String>()
        val updateJob = launch {
            todos.items[initialId]?.title?.collect { updatedTitles.add(it) }
        }

        // Update title of the TodoItem
        val updatedTitle = "Updated Title"
        todoRepository.editTitle(initialId, updatedTitle)
        advanceUntilIdle()
        assertTrue(updatedTitles.contains(updatedTitle))
        updateJob.cancel()

        // Test behavior after disposing of the list
        val disposedTitles = mutableListOf<String>()
        val disposedJob = launch {
            todos.items[initialId]?.title?.collect { disposedTitles.add(it) }
        }
        val removedItemsJob = launch {
            todos.itemRemoved.collect { id ->
                todos.items[id]?.title?.value?.let { disposedTitles.add(it) }
            }
        }

        // Dispose and attempt updates
        todos.dispose()
        todoRepository.editTitle(initialId, initialTitle)
        todoRepository.createNewTodoItem("Should Not Be Added", "", noDateType, 0, false)
        advanceUntilIdle()

        // Verify no updates after dispose
        assertFalse(disposedTitles.contains(initialTitle))
        disposedJob.cancel()
        removedItemsJob.cancel()
    }

}
