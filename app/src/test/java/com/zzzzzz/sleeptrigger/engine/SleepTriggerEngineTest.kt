package com.zzzzzz.sleeptrigger.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SleepTriggerEngineTest {
    @Test
    fun simulatedSleepSchedulesMediaPauseAfterDelay() {
        val repository = InMemoryTaskEventRepository()
        val scheduler = RecordingTaskScheduler()
        val engine = SleepTriggerEngine(
            eventRepository = repository,
            taskScheduler = scheduler,
            clock = FakeClock(1_000),
            idFactory = SequentialIdFactory()
        )

        val taskRun = engine.scheduleSimulatedSleepMediaPause(300_000)

        assertEquals("task-2", taskRun.id)
        assertEquals(301_000, taskRun.scheduledForMillis)
        assertEquals(TaskRunStatus.SCHEDULED, taskRun.status)
        assertEquals(1, repository.readAll().size)
        assertEquals(1, scheduler.scheduled.size)
        assertEquals("trigger-1", scheduler.scheduled.single().triggerEventId)
    }

    @Test
    fun negativeDelayIsRejected() {
        val engine = SleepTriggerEngine(
            eventRepository = InMemoryTaskEventRepository(),
            taskScheduler = RecordingTaskScheduler(),
            clock = FakeClock(1_000),
            idFactory = SequentialIdFactory()
        )

        assertThrows(IllegalArgumentException::class.java) {
            engine.scheduleSimulatedSleepMediaPause(-1)
        }
    }
}

