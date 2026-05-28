package com.zzzzzz.sleeptrigger.health

import com.zzzzzz.sleeptrigger.engine.DefaultTriggerDefinitions
import com.zzzzzz.sleeptrigger.engine.FakeClock
import com.zzzzzz.sleeptrigger.engine.InMemoryTaskEventRepository
import com.zzzzzz.sleeptrigger.engine.InMemoryTriggerDefinitionRepository
import com.zzzzzz.sleeptrigger.engine.RecordingTaskScheduler
import com.zzzzzz.sleeptrigger.engine.SequentialIdFactory
import com.zzzzzz.sleeptrigger.engine.SleepTriggerEngine
import com.zzzzzz.sleeptrigger.engine.TriggerRouter
import com.zzzzzz.sleeptrigger.engine.TriggerSource
import com.zzzzzz.sleeptrigger.engine.TriggerType
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepSessionTriggerImporterTest {
    @Test
    fun importsCompletedSleepSessionsAsAsleepTriggers() {
        val repository = InMemoryTaskEventRepository()
        val scheduler = RecordingTaskScheduler()
        val importedStore = InMemoryImportedSleepSessionStore()
        val importer = SleepSessionTriggerImporter(
            sleepImporter = FakeHealthConnectSleepImporter(
                listOf(
                    CompletedSleepSession(
                        id = "sleep-1",
                        startMillis = 1_000,
                        endMillis = 20_000,
                        sourcePackage = "com.example.sleep"
                    )
                )
            ),
            importedStore = importedStore,
            triggerRouter = TriggerRouter(
                definitionRepository = InMemoryTriggerDefinitionRepository(
                    listOf(DefaultTriggerDefinitions.sleepPauseMedia)
                ),
                sleepTriggerEngine = SleepTriggerEngine(
                    eventRepository = repository,
                    taskScheduler = scheduler,
                    clock = FakeClock(30_000),
                    idFactory = SequentialIdFactory()
                )
            )
        )

        val result = importer.importAndRoute(sinceMillis = 0)

        assertEquals(1, result.importedSessions)
        assertEquals(1, result.routedEvents)
        assertEquals(1, scheduler.scheduled.size)
        val event = repository.readAll().single()
        assertEquals(TriggerType.ASLEEP_DETECTED, event.triggerEvent.triggerType)
        assertEquals(TriggerSource.HEALTH_CONNECT, event.triggerEvent.source)
        assertEquals("sleep-1", event.triggerEvent.metadata["healthConnectSleepSessionId"])
        assertEquals("com.example.sleep", event.triggerEvent.metadata["sourcePackage"])
        assertEquals(330_000, event.taskRun.scheduledForMillis)
    }

    @Test
    fun doesNotImportTheSameSleepSessionTwice() {
        val repository = InMemoryTaskEventRepository()
        val scheduler = RecordingTaskScheduler()
        val importedStore = InMemoryImportedSleepSessionStore()
        val triggerImporter = SleepSessionTriggerImporter(
            sleepImporter = FakeHealthConnectSleepImporter(
                listOf(CompletedSleepSession("sleep-1", 1_000, 20_000, "com.example.sleep"))
            ),
            importedStore = importedStore,
            triggerRouter = TriggerRouter(
                definitionRepository = InMemoryTriggerDefinitionRepository(
                    listOf(DefaultTriggerDefinitions.sleepPauseMedia)
                ),
                sleepTriggerEngine = SleepTriggerEngine(
                    eventRepository = repository,
                    taskScheduler = scheduler,
                    clock = FakeClock(30_000),
                    idFactory = SequentialIdFactory()
                )
            )
        )

        triggerImporter.importAndRoute(sinceMillis = 0)
        val secondResult = triggerImporter.importAndRoute(sinceMillis = 0)

        assertEquals(0, secondResult.importedSessions)
        assertEquals(0, secondResult.routedEvents)
        assertEquals(1, repository.readAll().size)
        assertEquals(1, scheduler.scheduled.size)
    }
}

private class FakeHealthConnectSleepImporter(
    private val sessions: List<CompletedSleepSession>
) : HealthConnectSleepImporter {
    override fun importCompletedSleepSessions(sinceMillis: Long): List<CompletedSleepSession> {
        return sessions.filter { session -> session.endMillis >= sinceMillis }
    }
}

private class InMemoryImportedSleepSessionStore : ImportedSleepSessionStore {
    private val imported = mutableSetOf<String>()

    override fun hasImported(sessionId: String): Boolean = imported.contains(sessionId)

    override fun markImported(sessionId: String) {
        imported += sessionId
    }
}
