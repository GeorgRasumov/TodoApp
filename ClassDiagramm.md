```mermaid
---
config:
  layout: elk
  theme: base
---

classDiagram
direction RL

    class TodoItem {
        + id
        + title
        + description
        + dateType
        + position
        + isCompleted
        + repetitiveTodoId
    }

    class TodoItemFactory{
        + createNewTodoItem()
    }

    class TodoRepository{
        + createNewTodoItem()
        + deleteTodoItem()
        + getTodoItems(dateType)
        + getTodoItem(id)
        + editTitle()
        + editDescription(id, newTitle)
        + editDateType(id, newDateType)
        + editPosition(id, newPosition)
        + editIsCompleted(id, isCompleted)
    }

    class TodoService{
        + onTodoCreatedEvent
        + onTodoDeletedEvent
        + onTodoEditedEvent
        + getTodoItemsByDateType()
        + getTodoItemById()
        + setActiveDateType(DateType)
        + createNewTodoItem()
        + deleteTodoItem()
        + updatedTodo(TodoItem)
        + updatePositions()
    }

    class RepetitiveTodoService{
        + onRepetitiveTodoCreatedEvent
        + onRepetitiveTodoDeletedEvent
        + onRepetitiveTodoEditedEvent
        + getRepetitiveTodoByType()
        + getAllRepetitiveTodos()
        + getRepetitiveTodoById()
        + getTodosForDate(DateType)
        + createNewRepetitiveTodo()
        + addCreatedTodoToRepetitiveTodo()
        + deleteRepetitiveTodo()
        + updateRepetitiveTodo()
        + updatePositions()
    }


    class RepetitiveTodo{
        + id
        + title
        + description
        + position
        + type
        + interval
        + weekdays
        + startDate
        + createdTodos
    }

    class RepetitiveTodoRepository{
        + createNewRepetitiveTodo()
        + deleteRepetitiveTodo()
        + getRepetitiveTodos()
        + getAllRepetitiveTodos()
        + getRepetitiveTodo()
        + editTitle()
        + editDescription()
        + editRepetitionType()
        + editPosition()
        + editInterval()
        + editWeekdays()
        + editStartDate()
        + editCreatedTodos()
    }


    class MainActivity{

    }

    class MainViewModel{

    }

    class TodoItemListView{

    }

    class TodoItemListViewModel{

    }

    class TodoItemView{

    }

    class TodoItemViewModel{

    }

    class RepetitiveTodoListView{

    }

    class RepetitiveTodoListViewModel{

    }

    class RepetitiveTodoView{

    }

    class RepetitiveTodoViewModel{

    }

    class CalenderView{

    }

    class CalenderViewModel{

    }

    %% Relationships
CalenderView --> CalenderViewModel : observes & calls
CalenderView --> TodoItemListView : creates & destroys
MainActivity --> MainViewModel : observes & calls
MainActivity --> CalenderView : creates & destroys
MainActivity --> TodoItemListView : creates & destroys
MainActivity --> RepetitiveTodoListView : creates & destroys
RepetitiveTodoListView --> RepetitiveTodoListViewModel : observes & calls
RepetitiveTodoListView --> RepetitiveTodoView : creates
RepetitiveTodoListViewModel --> RepetitiveTodoService : observes & calls
RepetitiveTodoView --> RepetitiveTodoViewModel : observes & calls
RepetitiveTodoViewModel --> RepetitiveTodoListViewModel : observes & calls
TodoItemView --> TodoItemViewModel : observes & calls
TodoItemViewModel --> TodoItemListViewModel : observes & calls
TodoItemListView --> TodoItemView : creates
TodoItemListView --> TodoItemListViewModel : observes & calls

TodoItemListViewModel --> TodoService : observes & calls
TodoService --> TodoRepository : observes & calls
TodoService --> RepetitiveTodoService : observes & calls
RepetitiveTodoService --> RepetitiveTodoRepository : observes & calls
