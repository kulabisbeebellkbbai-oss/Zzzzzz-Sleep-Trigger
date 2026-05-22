package com.zzzzzz.sleeptrigger.engine

import com.zzzzzz.sleeptrigger.media.MediaPauseResult
import com.zzzzzz.sleeptrigger.media.MediaPauser
import com.zzzzzz.sleeptrigger.store.TaskEventRepository

class FakeClock(var value: Long) : Clock {
    override fun nowMillis(): Long = value
}

class SequentialIdFactory : IdFactory {
    private var next = 0
    override fun newId(prefix: String): String {
        next += 1
        return "$prefix-$next"
    }
}

class RecordingTaskScheduler : TaskScheduler {
    val scheduled = mutableListOf<ScheduledTaskRequest>()
    val canceled = mutableListOf<String>()

    override fun schedule(request: ScheduledTaskRequest) {
        scheduled += request
    }

    override fun cancel(taskRunId: String) {
        canceled += taskRunId
    }
}

class InMemoryTaskEventRepository : TaskEventRepository {
    private val events = mutableListOf<LoggedTaskEvent>()

    override fun append(triggerEvent: TriggerEvent, taskRun: TaskRun) {
        events += LoggedTaskEvent(triggerEvent, taskRun)
    }

    override fun updateTaskRun(taskRun: TaskRun) {
        val index = events.indexOfFirst { it.taskRun.id == taskRun.id }
        if (index >= 0) {
            events[index] = events[index].copy(taskRun = taskRun)
        }
    }

    override fun findTaskRun(id: String): TaskRun? {
        return events.firstOrNull { it.taskRun.id == id }?.taskRun
    }

    override fun readAll(): List<LoggedTaskEvent> = events.toList()
}

class FakeMediaPauser(private val result: MediaPauseResult) : MediaPauser {
    var calls = 0

    override fun pauseActiveSessions(): MediaPauseResult {
        calls += 1
        return result
    }
}

