package com.zzzzzz.sleeptrigger.engine

import com.zzzzzz.sleeptrigger.media.MediaPauseResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskRunExecutorTest {
    @Test
    fun executesScheduledMediaPauseAndRecordsSuccess() {
        val repository = InMemoryTaskEventRepository()
        repository.append(
            triggerEvent = TriggerEvent("trigger-1", TriggerType.ASLEEP_DETECTED, TriggerSource.SIMULATED, 1_000, 1.0f),
            taskRun = TaskRun("task-1", "trigger-1", TaskType.PAUSE_MEDIA, 1_500, status = TaskRunStatus.SCHEDULED)
        )
        val clock = FakeClock(2_000)
        val mediaPauser = FakeMediaPauser(
            MediaPauseResult(listOf("music.app"), emptyList(), "Paused 1 active media session(s).", true)
        )

        val completed = TaskRunExecutor(repository, mediaPauser, clock).execute("task-1")

        assertEquals(1, mediaPauser.calls)
        assertEquals(TaskRunStatus.SUCCEEDED, completed?.status)
        assertEquals("Paused 1 active media session(s).", completed?.resultMessage)
        assertEquals(TaskRunStatus.SUCCEEDED, repository.findTaskRun("task-1")?.status)
    }

    @Test
    fun missingTaskRunReturnsNull() {
        val completed = TaskRunExecutor(
            eventRepository = InMemoryTaskEventRepository(),
            mediaPauser = FakeMediaPauser(MediaPauseResult(emptyList(), emptyList(), "unused", true)),
            clock = FakeClock(2_000)
        ).execute("missing")

        assertNull(completed)
    }
}

