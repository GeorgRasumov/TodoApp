package com.georg.todoapp.data.todos

import com.georg.todoapp.data.DateType
import kotlinx.coroutines.flow.StateFlow

interface IReadOnlyTodoItem {
    val title: StateFlow<String>
    val dateType: StateFlow<DateType>
    val description: StateFlow<String>
    val position: StateFlow<Int>
    val isCompleted: StateFlow<Boolean>
}
