package com.zzzzzz.sleeptrigger.store

import com.zzzzzz.sleeptrigger.engine.LoggedTaskEvent
import com.zzzzzz.sleeptrigger.engine.TaskRun
import com.zzzzzz.sleeptrigger.engine.TriggerEvent

interface TaskEventRepository {
    fun append(triggerEvent: TriggerEvent, taskRun: TaskRun)
    fun updateTaskRun(taskRun: TaskRun)
    fun findTaskRun(id: String): TaskRun?
    fun readAll(): List<LoggedTaskEvent>
}

