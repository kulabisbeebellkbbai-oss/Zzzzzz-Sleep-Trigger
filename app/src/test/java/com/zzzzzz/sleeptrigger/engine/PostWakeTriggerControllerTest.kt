package com.zzzzzz.sleeptrigger.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class PostWakeTriggerControllerTest {
    @Test
    fun routesStoodUpAfterWakeOnceDebounced() {
        val repository = InMemoryTaskEventRepository()
        val scheduler = RecordingTaskScheduler()
        val controller = PostWakeTriggerController(
            detector = WakeStandUpDetector(debounceMillis = 30_000),
            triggerRouter = TriggerRouter(
                definitionRepository = InMemoryTriggerDefinitionRepository(
                    listOf(DefaultTriggerDefinitions.stoodUpAfterWake)
                ),
                sleepTriggerEngine = SleepTriggerEngine(
                    eventRepository = repository,
                    taskScheduler = scheduler,
                    clock = FakeClock(50_000),
                    idFactory = SequentialIdFactory()
                )
            )
        )

        controller.onWakeDetected(1_000)
        assertEquals(emptyList<LoggedTaskEvent>(), controller.onMovement(10_000, 3, PostWakeActivity.WALKING))
        val routed = controller.onMovement(41_000, 3, PostWakeActivity.WALKING)

        assertEquals(1, routed.size)
        assertEquals(TriggerType.STOOD_UP_AFTER_WAKE, routed.single().triggerEvent.triggerType)
        assertEquals(50_000, routed.single().taskRun.scheduledForMillis)
        assertEquals(1, scheduler.scheduled.size)
    }
}

