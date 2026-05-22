package com.zzzzzz.sleeptrigger.engine

import com.zzzzzz.sleeptrigger.wear.WearTriggerPayload
import org.junit.Assert.assertEquals
import org.junit.Test

class TriggerRouterTest {
    @Test
    fun routesWearSleepPayloadThroughEnabledDefinition() {
        val repository = InMemoryTaskEventRepository()
        val scheduler = RecordingTaskScheduler()
        val router = TriggerRouter(
            definitionRepository = InMemoryTriggerDefinitionRepository(
                listOf(DefaultTriggerDefinitions.sleepPauseMedia)
            ),
            sleepTriggerEngine = SleepTriggerEngine(
                eventRepository = repository,
                taskScheduler = scheduler,
                clock = FakeClock(10_000),
                idFactory = SequentialIdFactory()
            )
        )

        val routed = router.routeWearPayload(
            WearTriggerPayload(
                eventId = "wear-1",
                triggerType = TriggerType.ASLEEP_DETECTED,
                source = TriggerSource.WEAR_HEALTH_SERVICES,
                detectedAtMillis = 9_500,
                confidence = 0.9f
            )
        )

        assertEquals(1, routed.size)
        assertEquals(310_000, routed.single().taskRun.scheduledForMillis)
        assertEquals("sleep-pause-media", routed.single().triggerEvent.metadata["triggerDefinitionId"])
        assertEquals("wear-1", routed.single().triggerEvent.metadata["wearEventId"])
        assertEquals(1, scheduler.scheduled.size)
    }

    @Test
    fun disabledDefinitionsDoNotRoute() {
        val router = TriggerRouter(
            definitionRepository = InMemoryTriggerDefinitionRepository(
                listOf(DefaultTriggerDefinitions.sleepPauseMedia.copy(enabled = false))
            ),
            sleepTriggerEngine = SleepTriggerEngine(
                eventRepository = InMemoryTaskEventRepository(),
                taskScheduler = RecordingTaskScheduler(),
                clock = FakeClock(10_000),
                idFactory = SequentialIdFactory()
            )
        )

        val routed = router.routeTrigger(TriggerType.ASLEEP_DETECTED, TriggerSource.SIMULATED, 1.0f)

        assertEquals(emptyList<LoggedTaskEvent>(), routed)
    }
}

