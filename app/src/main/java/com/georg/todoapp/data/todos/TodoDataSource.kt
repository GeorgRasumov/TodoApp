package com.georg.todoapp.data.todos

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.georg.todoapp.data.DateType
import com.georg.todoapp.db.Database
import com.georg.todoapp.db.SqlTodoItem
import com.georg.todoapp.db.TodoDatabaseQueries
import java.time.LocalDate

class DateTypeAdapter : ColumnAdapter<DateType, String> {
    // Convert from SQLite value to DateType
    override fun decode(databaseValue: String): DateType {
        val parts = databaseValue.split("|") // Delimiter for serialization
        val type = DateType.Type.valueOf(parts[0]) // First part is the Type enum
        val date = if (parts.size > 1 && parts[1].isNotEmpty()) LocalDate.parse(parts[1]) else null
        return DateType.getInstance(type, date)
    }

    // Convert from DateType to SQLite-compatible value
    override fun encode(value: DateType): String {
        val datePart = value.date?.toString() ?: "" // Serialize LocalDate as ISO string
        return "${value.type.name}|$datePart"
    }
}

class TodoDataSource(
    driver: SqlDriver,
) : ITodoDataSource {
    // private val driver: SqlDriver = AndroidSqliteDriver(Database.Schema, context, "Database.db")
    private val database = Database(driver, SqlTodoItemAdapter = SqlTodoItem.Adapter(dateTypeAdapter = DateTypeAdapter()))
    private val todoQueries: TodoDatabaseQueries = database.todoDatabaseQueries

    override fun getTodoItem(id: Int): TodoItem? {
        val sqlTodoItem = todoQueries.getTodoItemById(id.toLong()).executeAsOneOrNull() ?: return null
        return sqlTodoItemToTodoItem(sqlTodoItem)
    }

    override fun addTodoItem(todoItem: IReadOnlyTodoItem) {
        todoQueries.insertTodoItem(
            id = todoItem.id.toLong(),
            title = todoItem.title.value,
            description = todoItem.description.value,
            dateType = todoItem.dateType.value,
            position = todoItem.position.value.toLong(),
            isCompleted = todoItem.isCompleted.value,
        )
    }

    override fun removeTodoItem(id: Int): Boolean {
        todoQueries.deleteTodoItem(id.toLong())
        return true // SQLDelight doesn't return affected rows; assume success
    }

    override fun getTodoItems(dateType: DateType): List<TodoItem> {
        val sqlItems =
            todoQueries
                .getTodoItemsByDateType(
                    dateType = dateType,
                ).executeAsList()
        return sqlItems.map { sqlTodoItemToTodoItem(it) }
    }

    override fun updateTitle(
        id: Int,
        title: String,
    ): Boolean {
        todoQueries.updateTitle(title = title, id = id.toLong())
        return true // Assume success
    }

    override fun updateDescription(
        id: Int,
        description: String,
    ): Boolean {
        todoQueries.updateDescription(description = description, id = id.toLong())
        return true // Assume success
    }

    override fun updateDateType(
        id: Int,
        dateType: DateType,
    ): Boolean {
        todoQueries.updateDateType(dateType = dateType, id = id.toLong())
        return true // Assume success
    }

    override fun updatePosition(
        id: Int,
        position: Int,
    ): Boolean {
        todoQueries.updatePosition(position = position.toLong(), id = id.toLong())
        return true // Assume success
    }

    override fun updateIsCompleted(
        id: Int,
        isCompleted: Boolean,
    ): Boolean {
        todoQueries.updateIsCompleted(isCompleted = isCompleted, id = id.toLong())
        return true // Assume success
    }

    private fun sqlTodoItemToTodoItem(sqlTodoItem: SqlTodoItem): TodoItem =
        TodoItem(
            id = sqlTodoItem.id.toInt(),
            title = sqlTodoItem.title,
            description = sqlTodoItem.description ?: "",
            dateType = sqlTodoItem.dateType,
            position = sqlTodoItem.position.toInt(),
            isCompleted = sqlTodoItem.isCompleted,
        )
}
