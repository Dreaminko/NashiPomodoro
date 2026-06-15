package com.dreaminko.nashipomodoro.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreaminko.nashipomodoro.R
import com.dreaminko.nashipomodoro.domain.model.TaskItem
import com.dreaminko.nashipomodoro.ui.components.PageTitle

@Composable
fun TaskListScreen(
    onFocusTask: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val visibleTasks = remember(tasks, filter) { tasks.filteredBy(filter) }
    val openCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count(TaskItem::isCompleted)
    var title by remember { mutableStateOf("") }
    var editingTask by remember { mutableStateOf<TaskItem?>(null) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    fun addTask() {
        if (title.isBlank()) return
        viewModel.add(title)
        title = ""
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            PageTitle(stringResource(R.string.nav_tasks))
            Text(
                stringResource(
                    R.string.tasks_summary,
                    pluralStringResource(
                        R.plurals.tasks_open_count,
                        openCount,
                        openCount
                    ),
                    pluralStringResource(
                        R.plurals.tasks_completed_count,
                        completedCount,
                        completedCount
                    )
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text(stringResource(R.string.tasks_input_placeholder)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addTask() })
                )
                Button(
                    onClick = ::addTask,
                    enabled = title.isNotBlank(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(stringResource(R.string.action_add))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskFilter.entries.forEach { item ->
                FilterChip(
                    selected = filter == item,
                    onClick = { viewModel.setFilter(item) },
                    label = { Text(stringResource(item.labelRes)) }
                )
            }
        }

        if (visibleTasks.isEmpty()) {
            EmptyTaskState(hasTasks = tasks.isNotEmpty(), modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visibleTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { viewModel.toggle(task) },
                        onFocus = {
                            viewModel.selectForFocus(task)
                            onFocusTask()
                        },
                        onEdit = { editingTask = task },
                        onDelete = { viewModel.delete(task) }
                    )
                }
            }
        }

        if (completedCount > 0) {
            TextButton(
                onClick = { showClearConfirmation = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.tasks_clear_completed))
            }
        }
    }

    editingTask?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { editingTask = null },
            onSave = { editedTitle, description, goal ->
                viewModel.update(task, editedTitle, description, goal)
                editingTask = null
            }
        )
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text(stringResource(R.string.tasks_clear_completed)) },
            text = { Text(stringResource(R.string.tasks_clear_completed_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCompleted()
                    showClearConfirmation = false
                }) {
                    Text(stringResource(R.string.action_remove))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyTaskState(hasTasks: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(
                    if (hasTasks) R.string.tasks_filter_empty else R.string.tasks_empty_title
                ),
                style = MaterialTheme.typography.titleLarge
            )
            if (!hasTasks) {
                Text(
                    stringResource(R.string.tasks_empty_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskItem,
    onToggle: () -> Unit,
    onFocus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() }
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                task.description?.takeIf(String::isNotBlank)?.let { description ->
                    Text(
                        description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                LinearProgressIndicator(
                    progress = {
                        (task.pomodoroDone.toFloat() / task.pomodoroGoal)
                            .coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    pluralStringResource(
                        R.plurals.tasks_session_progress,
                        task.pomodoroGoal,
                        task.pomodoroDone,
                        task.pomodoroGoal
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!task.isCompleted) {
                IconButton(onClick = onFocus) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = stringResource(R.string.tasks_focus, task.title)
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = stringResource(R.string.tasks_edit)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(R.string.action_remove)
                )
            }
        }
    }
}

@Composable
private fun EditTaskDialog(
    task: TaskItem,
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    var title by remember(task.id) { mutableStateOf(task.title) }
    var description by remember(task.id) { mutableStateOf(task.description.orEmpty()) }
    var goal by remember(task.id) { mutableIntStateOf(task.pomodoroGoal.coerceIn(1, 99)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.task_detail_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.tasks_title)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.tasks_description)) },
                    minLines = 3,
                    maxLines = 5
                )
                Text(
                    stringResource(R.string.tasks_goal),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = { goal-- },
                        enabled = goal > 1
                    ) {
                        Icon(
                            Icons.Rounded.Remove,
                            contentDescription = stringResource(R.string.tasks_decrease_goal)
                        )
                    }
                    Text(
                        pluralStringResource(R.plurals.tasks_goal_rounds, goal, goal),
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(
                        onClick = { goal++ },
                        enabled = goal < 99
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.tasks_increase_goal)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title, description, goal) },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

private val TaskFilter.labelRes: Int
    get() = when (this) {
        TaskFilter.ALL -> R.string.tasks_filter_all
        TaskFilter.OPEN -> R.string.tasks_filter_open
        TaskFilter.COMPLETED -> R.string.tasks_filter_completed
    }
