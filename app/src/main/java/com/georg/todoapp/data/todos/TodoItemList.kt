package com.georg.todoapp.data.todos

import com.georg.todoapp.data.DateType
import com.georg.todoapp.utils.IChangeNotifier
import com.georg.todoapp.utils.MutableObservableList

class TodoItemList(
    val dateType: DateType,
) : MutableObservableList<Int, TodoItem, IReadOnlyTodoItem>(),
    ReadOnlyTodoItemList {
    private var changeNotifier: IChangeNotifier<DateType, TodoItemList>? = null

    fun setChangeNotifier(newChangeNotifier: IChangeNotifier<DateType, TodoItemList>) {
        changeNotifier?.removeListener(dateType, this)
        changeNotifier = newChangeNotifier
        changeNotifier!!.addListener(dateType, this)
    }

    override fun dispose() {
        changeNotifier?.removeListener(dateType, this)
        items.forEach {
            it.value.dispose()
        }
    }
}
