# 1. Goal
- create yet another todo app
- it should be simple but not as simple as most todo apps out there
- the main idea is to work with a day-list that gets created for every day
- aswell as with a list with untimed todos
- it should be very easy to create a todo

# 2. Technical Specifications
- use Kotlin
- primarly for android
- with possiblity to convert to other systems
- always use native UI
- ues MVVM-archtecture.
- model should not use android specific packages
- when model need android specific functions use dependency injection

# 3. Functions

## 3.1. Views
- use Sidebar to switch views

### 3.1.1 Base-List-View

- lists todos of a specific day or category
- also used for repetitive todos
- can be in work mode or edit mode

#### Normal mode
- on the right every todo has an "edit button"
- tapping a todo toggles "done" (except when repetitive todo)
  - when a todo with a project is done a window pops up asking: open project?
- holding it for long enabled multiple select
   - setting the date for multiple todos (except when repetitive todo)
   - deleting multiple todos
 - when a todo has a project it is written small under the todo
   - on the left project todos have a button to "go to project"

#### Edit mode
- activated by button on the top
- deactivated by pressing "Back"
- on the left every todo gets a move button
- on the right every todo gets a delete button

#### Add Button

- alway visible on the bottom of every type of todo list
- opens the todo menu with the list being the default setting
- is floating above the list

### 3.1.2 Todays Todos
- shows all todays todos aswell as overdue todos
- section for overdue at the bottom
 - pressing an overdue todo will add it to today

### 3.1.3 Tomorrows Todos
 - show all todos for tomorrow
 - section for overdue at the bottom
 - pressing an overdue todo will add it to tomorrow

### 3.1.4 Calendar
 - show calendar with all days
 - show todos that are not hidden in calendar
 - pressing a day opens the todo list for the day

### 3.1.5 Non repetitive todos
 - list todos that are only due once
 - has different pages:
     - unscheduled
     - flexible
     - waiting
     - scheduled
  
### 3.1.6 Repetitive Todo
 - page for creating and editing repetitive todos
 - filter for repetition type

### 3.1.7 Projects
 - view for creating editing and viewing projects
 - filter projects
   - no todos
   - unsheduled
   - scheduled
   - halted
  
### Settings
 - day change time
 - show project popup?
     - ask wheter to show project when project todo is done

## 3.2 Popups
### Edit and Create Todo Popup
 - opens when todo is created or edited
 - presetting depend on where the edit button is pressed

 - the following things can be set:
 - todo types:
   - Scheduled (has due date)
   - Unscheduled
   - Halted
   - Is Waiting
 - Flexible
 - Project
 - Hide in Calendar
 - push to next day
 - Color

### Edit and Create Repetitive Todo Popup
 - opens when a repetitive todo is created or edited
 - repetition type
	- days
	- weekdays
	- day of month
  - day of year
 - repetion times
 - Hide in Calendar
 - push to next day
 - Color


## 3.3 Mechanics
### Repetitive todos
 - repetitive todos can be edited as every other todo
 - when the day is changes no new todo will be created for that day
 - when an repetitive todo gets deleted all its instances will be deleted aswell
   - except when the title was changed or the todo is already done 

- Wiederkehrende Todos erstellen automatisch in festgelegten Abständen neue Todos.
- Die Erstellung erfolgt bis zu dem Datum, das der Nutzer im Kalender einsehen kann.
- Aus wiederkehrenden Todos generierte Todos haben dieselbe Funktionalität wie normale Todos:
  - Sie können umbenannt und verschoben werden.
- Werden wiederkehrende Todos gelöscht, werden alle daraus erstellten offenen Todos gelöscht (auch bearbeitete).
  - Abgehakte Todos bleiben erhalten.
  - Gleiches gilt, wenn Startdatum oder Wiederholabstand geändert wird.
