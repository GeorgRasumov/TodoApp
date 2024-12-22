package com.georg.todoapp

import com.georg.todoapp.data.DateType
import com.georg.todoapp.data.todos.ITodoDataSource
import com.georg.todoapp.data.IUniqueIdProvider
import com.georg.todoapp.data.todos.ReadOnlyTodoItemList
import com.georg.todoapp.data.todos.TodoItem
import com.georg.todoapp.data.todos.TodoItemList
import com.georg.todoapp.data.todos.TodoRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate


@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TodoRepositoryTest {

    private lateinit var testScope: TestScope
    private lateinit var todoRepository: TodoRepository

    private lateinit var uniqueIdProvider: IUniqueIdProvider
    private lateinit var todoDataSource: ITodoDataSource

    private val initTodoItem1 =
        TodoItem(0, "Title1", DateType.getInstance(DateType.Type.NO_DATE), "Description1", 0, false)
    private val initTodoItem2 =
        TodoItem(1, "Title2", DateType.getInstance(DateType.Type.NO_DATE), "Description2", 1, false)
    private val testTodoItem1 =
        TodoItem(2, "Title3", DateType.getInstance(DateType.Type.NO_DATE), "Description3", 2, false)
    private val testTodoItem2 =
        TodoItem(3, "Title4", DateType.getInstance(DateType.Type.NO_DATE),"Description4", 3,false)

    private val editedTodoItem =
        TodoItem(4, "Title5", DateType.getInstance(DateType.Type.FIXED_DATE, LocalDate.MIN),"Description5", 0,true)


    @Before
    fun setUp() {
        testScope = TestScope()


        val todoItemList = TodoItemList(DateType.getInstance(DateType.Type.NO_DATE))
        todoItemList.add(initTodoItem1.id, initTodoItem1.copy())
        todoItemList.add(initTodoItem2.id, initTodoItem2.copy())

        todoDataSource = mock(ITodoDataSource::class.java)
        `when`(todoDataSource.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))).thenReturn(
            todoItemList
        )
        `when`(todoDataSource.getTodoItems(editedTodoItem.dateType.value)).thenReturn(
            TodoItemList(editedTodoItem.dateType.value)
        )
        `when`(todoDataSource.getTodoItem(initTodoItem1.id)).thenReturn(
            initTodoItem1.copy()
        )
        `when`(todoDataSource.getTodoItem(initTodoItem2.id)).thenReturn(
            initTodoItem2.copy()
        )

        uniqueIdProvider = mock(IUniqueIdProvider::class.java)
        var currentId = 2
        `when`(uniqueIdProvider.getUniqueId()).thenAnswer {
            currentId++
        }

        todoRepository = TodoRepository(uniqueIdProvider, todoDataSource, testScope)
    }

    private fun addTestTodoItemToRepository(todoItem: TodoItem) {
        todoRepository.createNewTodoItem(
            todoItem.title.value,
            todoItem.description.value,
            todoItem.dateType.value,
            todoItem.position.value,
            todoItem.isCompleted.value
        )
    }



    @Test
    fun `Method createNewTodoItem`() = runTest {

        val todoItemList = todoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        val testCollector = TodoItemTestCollector(todoItemList)
        testCollector.startCollecting(this)

        addTestTodoItemToRepository(testTodoItem1)

        advanceUntilIdle()
        testCollector.stopCollecting()

        assertEquals(3, todoItemList.items.size) // check if the new item was added
        assertTrue(testCollector.collectedItemsAdded.contains(testTodoItem1.id)) // check if the Event was triggered

    }


    @Test
    fun `Method deleteTodoItem already existing`() = runTest {

        val todoItemList = todoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        val testCollector = TodoItemTestCollector(todoItemList)
        testCollector.startCollecting(this)

        todoRepository.deleteTodoItem(initTodoItem1.id)

        advanceUntilIdle()
        testCollector.stopCollecting()

        assertEquals(1, todoItemList.items.size)
        assertTrue(testCollector.collectedItemsRemoved.contains(initTodoItem1.id)) // check if the Event was triggered

    }

    @Test
    fun `Method deleteTodoItem created`() = runTest {

        val todoItemList = todoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        val testCollector = TodoItemTestCollector(todoItemList)
        testCollector.startCollecting(this)

        addTestTodoItemToRepository(testTodoItem1)
        todoRepository.deleteTodoItem(testTodoItem1.id)

        advanceUntilIdle()
        testCollector.stopCollecting()

        assertEquals(2, todoItemList.items.size)
        assertTrue(testCollector.collectedItemsRemoved.contains(testTodoItem1.id)) // check if the Event was triggered

    }

    @Test
    fun `edit TodoItems`() = runTest {

        val todoItemListNoDate = todoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        val testCollector = TodoItemTestCollector(todoItemListNoDate)
        testCollector.startCollecting(this)

        todoRepository.editDescription(initTodoItem1.id, editedTodoItem.description.value)
        todoRepository.editTitle(initTodoItem1.id, editedTodoItem.title.value)
        todoRepository.editIsCompleted(initTodoItem1.id, editedTodoItem.isCompleted.value)

        advanceUntilIdle()
        testCollector.stopCollecting()

        assertTrue(testCollector.collectedDescriptions.contains(editedTodoItem.description.value))
        assertTrue(testCollector.collectedTitles.contains(editedTodoItem.title.value))
        assertTrue(testCollector.collectedCompletionStates.contains(editedTodoItem.isCompleted.value))

    }

    @Test
    fun `Method UpdatePosition`() = runTest {

        val newPositionOfItem2 = 0

        val todoItemListNoDate = todoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        val testCollector = TodoItemTestCollector(todoItemListNoDate)
        testCollector.startCollecting(this)

        addTestTodoItemToRepository(testTodoItem1)
        addTestTodoItemToRepository(testTodoItem2)

        todoRepository.updatePosition(testTodoItem2.id, newPositionOfItem2)

        advanceUntilIdle()
        testCollector.stopCollecting()

        assertEquals(4, todoItemListNoDate.items.size)
        assertTrue(testCollector.collectedPositions.contains(newPositionOfItem2))
        assertEquals(1, testCollector.collectedPositionsChanged.size)
        assertEquals(todoRepository.getTodoItem(testTodoItem2.id)?.position?.value, newPositionOfItem2)
        assertEquals(todoRepository.getTodoItem(initTodoItem1.id)?.position?.value, initTodoItem1.position.value + 1) // check if the other item was moved up
    }

    @Test
    fun `Method ChangeDateType`() = runTest {

        val todoItemListNoDate = todoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        val todoItemListNewDate = todoRepository.getTodoItems(editedTodoItem.dateType.value)
        val testCollectorNoDate = TodoItemTestCollector(todoItemListNoDate)
        val testCollectorNewDate = TodoItemTestCollector(todoItemListNewDate)
        testCollectorNoDate.startCollecting(this)
        testCollectorNewDate.startCollecting(this)

        todoRepository.editDateType(initTodoItem1.id, editedTodoItem.dateType.value)

        advanceUntilIdle()
        testCollectorNoDate.stopCollecting()
        testCollectorNewDate.stopCollecting()

        assertEquals(1, todoItemListNoDate.items.size) // Date was changed so one item moved to another list
        assertEquals(1, todoItemListNewDate.items.size) // Date was changed so one item moved to another list
        assertEquals(1, testCollectorNoDate.collectedItemsRemoved.size) // check if the Event was triggered
        assertEquals(1, testCollectorNewDate.collectedItemsAdded.size) // check if the Event was triggered

    }


    @Test
    fun `test Cleanup of unused Items and Lists`() = runTest {
        val todoItemList = todoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        val testCollector = TodoItemTestCollector(todoItemList)
        testCollector.startCollecting(this)
        testScope.advanceTimeBy(100_000L)
        advanceUntilIdle()
        verify(todoDataSource).getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        clearInvocations(todoDataSource)

        todoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        verifyNoMoreInteractions(todoDataSource)

        testCollector.stopCollecting()
        advanceUntilIdle()
        testScope.advanceTimeBy(100_000L)

        todoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        verify(todoDataSource).getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
    }

    @Test
    fun `on Get Todo Event Callback`() = runTest {

        todoRepository.onGetTodosEvent.addListener { dateType ->
            todoRepository.createNewTodoItem(
                testTodoItem1.title.value,
                testTodoItem1.description.value,
                testTodoItem1.dateType.value,
                testTodoItem1.position.value,
                testTodoItem1.isCompleted.value
            )}

        val todoItemList = todoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))

        assertTrue(todoItemList.items.containsKey(testTodoItem1.id))
    }

}


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