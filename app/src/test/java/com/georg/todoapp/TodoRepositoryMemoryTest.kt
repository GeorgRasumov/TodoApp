package com.georg.todoapp

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.georg.todoapp.data.DateType
import com.georg.todoapp.data.IUniqueIdProvider
import com.georg.todoapp.data.todos.ITodoDataSource
import com.georg.todoapp.data.todos.TodoDataSource
import com.georg.todoapp.data.todos.TodoItem
import com.georg.todoapp.data.todos.TodoRepository
import com.georg.todoapp.db.Database
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDate


@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TodoRepositoryMemoryTest {

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
    private val testTodoItem2=
        TodoItem(3, "Title4", DateType.getInstance(DateType.Type.NO_DATE), "Description4", 3, false)

    private val editedTodoItem =
        TodoItem(4, "Title5", DateType.getInstance(DateType.Type.FIXED_DATE, LocalDate.MIN), "Description5", 0, true)


    @Before
    fun setUp() {
        testScope = TestScope()

        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Database.Schema.create(driver)
        todoDataSource = TodoDataSource(driver)
        todoDataSource.addTodoItem(initTodoItem1)
        todoDataSource.addTodoItem(initTodoItem2)

        uniqueIdProvider = Mockito.mock(IUniqueIdProvider::class.java)
        var currentId = 2
        Mockito.`when`(uniqueIdProvider.getUniqueId()).thenAnswer {
            currentId++
        }

        todoRepository = TodoRepository(uniqueIdProvider, todoDataSource, testScope)
    }


    @Test
    fun `get loaded Items`() = runTest {

        val todoItemList = todoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))

        assertEquals(2, todoItemList.items.size)
        assertTrue(todoItemList.items[initTodoItem2.id]?.title?.value == initTodoItem2.title.value)
    }

    @Test
    fun `Delte Item From DataBase`() = runTest {
        todoRepository.deleteTodoItem(initTodoItem1.id)

        //Create a new repository to reload the items from the database
        val newTodoRepository = TodoRepository(uniqueIdProvider, todoDataSource, testScope)

        val todoItemList = newTodoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        assertEquals(1, todoItemList.items.size)
        assertFalse(todoItemList.items.containsKey(initTodoItem1.id))
    }

    @Test
    fun `add TodoItem to DataBase`() = runTest {
        addTestTodoItemToRepository(todoRepository, testTodoItem1)

        //Create a new repository to reload the items from the database
        val newTodoRepository = TodoRepository(uniqueIdProvider, todoDataSource, testScope)

        val todoItemList = newTodoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        assertEquals(3, todoItemList.items.size)
        assertTrue(todoItemList.items.containsKey(testTodoItem1.id))
    }

    @Test
    fun `TodoItem edited`() = runTest {
        todoRepository.editTitle(initTodoItem1.id, editedTodoItem.title.value)
        todoRepository.editDescription(initTodoItem1.id, editedTodoItem.description.value)
        todoRepository.editDateType(initTodoItem1.id, editedTodoItem.dateType.value)
        todoRepository.updatePosition(initTodoItem1.id, editedTodoItem.position.value)
        todoRepository.editIsCompleted(initTodoItem1.id, editedTodoItem.isCompleted.value)

        //Create a new repository to reload the items from the database
        val newTodoRepository = TodoRepository(uniqueIdProvider, todoDataSource, testScope)

        val todoItemList = newTodoRepository.getTodoItems(editedTodoItem.dateType.value)
        val todoItem = todoItemList.items[initTodoItem1.id]
        assertEquals(editedTodoItem.title.value, todoItem?.title?.value)
        assertEquals(editedTodoItem.description.value, todoItem?.description?.value)
        assertEquals(editedTodoItem.dateType.value, todoItem?.dateType?.value)
        assertEquals(editedTodoItem.position.value, todoItem?.position?.value)
        assertEquals(editedTodoItem.isCompleted.value, todoItem?.isCompleted?.value)
    }

    @Test
    fun `edit Date and Position`() = runTest {
        addTestTodoItemToRepository(todoRepository, testTodoItem1)
        addTestTodoItemToRepository(todoRepository, testTodoItem2)
        todoRepository.editDateType(testTodoItem1.id, DateType.getInstance(DateType.Type.FIXED_DATE, LocalDate.MIN))
        todoRepository.updatePosition(testTodoItem2.id, 0)

        //Create a new repository to reload the items from the database
        val newTodoRepository = TodoRepository(uniqueIdProvider, todoDataSource, testScope)

        val todoItemListNoDate = newTodoRepository.getTodoItems(DateType.getInstance(DateType.Type.NO_DATE))
        val todoItemListFixedDate = newTodoRepository.getTodoItems(DateType.getInstance(DateType.Type.FIXED_DATE, LocalDate.MIN))

        assertEquals(3, todoItemListNoDate.items.size)
        assertEquals(1, todoItemListFixedDate.items.size)
        assertEquals(0, todoItemListNoDate.items[testTodoItem2.id]?.position?.value)
        assertEquals(1, todoItemListNoDate.items[initTodoItem1.id]?.position?.value)
        assertEquals(2, todoItemListNoDate.items[initTodoItem2.id]?.position?.value)

    }

}
