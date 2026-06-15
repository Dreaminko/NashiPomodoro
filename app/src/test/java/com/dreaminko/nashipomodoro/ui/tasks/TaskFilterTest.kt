package com.dreaminko.nashipomodoro.ui.tasks

import com.dreaminko.nashipomodoro.domain.model.TaskItem
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskFilterTest {
    private val openTask = task(id = 1, completed = false)
    private val completedTask = task(id = 2, completed = true)
    private val tasks = listOf(openTask, completedTask)

    @Test
    fun all_keepsEveryTask() {
        assertEquals(tasks, tasks.filteredBy(TaskFilter.ALL))
    }

    @Test
    fun open_keepsOnlyIncompleteTasks() {
        assertEquals(listOf(openTask), tasks.filteredBy(TaskFilter.OPEN))
    }

    @Test
    fun completed_keepsOnlyCompletedTasks() {
        assertEquals(listOf(completedTask), tasks.filteredBy(TaskFilter.COMPLETED))
    }

    private fun task(id: Long, completed: Boolean) = TaskItem(
        id = id,
        title = "Task $id",
        isCompleted = completed,
        createdAt = id
    )
}
