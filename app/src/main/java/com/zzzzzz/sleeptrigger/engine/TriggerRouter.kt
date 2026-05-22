package com.zzzzzz.sleeptrigger.engine

import com.zzzzzz.sleeptrigger.wear.WearTriggerPayload

class TriggerRouter(
    private val definitionRepository: TriggerDefinitionRepository,
    private val sleepTriggerEngine: SleepTriggerEngine,
    private val taskRunExecutor: TaskRunExecutor? = null
) {
    fun routeWearPayload(payload: WearTriggerPayload): List<LoggedTaskEvent> {
        payload.validate()
        return routeTrigger(
            triggerType = payload.triggerType,
            source = payload.source,
            confidence = payload.confidence,
            metadata = payload.metadata + ("wearEventId" to payload.eventId)
        )
    }

    fun routeTrigger(
        triggerType: TriggerType,
        source: TriggerSource,
        confidence: Float,
        metadata: Map<String, String> = emptyMap()
    ): List<LoggedTaskEvent> {
        return definitionRepository.readDefinitions()
            .filter { it.enabled && it.triggerType == triggerType }
            .map { definition ->
                val loggedEvent = sleepTriggerEngine.handleTrigger(
                    triggerType = triggerType,
                    source = source,
                    confidence = confidence,
                    delayMillis = definition.delayMillis,
                    taskType = definition.taskType,
                    metadata = metadata + ("triggerDefinitionId" to definition.id)
                )
                if (definition.delayMillis == 0L) {
                    taskRunExecutor?.execute(loggedEvent.taskRun.id)?.let { executedRun ->
                        loggedEvent.copy(taskRun = executedRun)
                    } ?: loggedEvent
                } else {
                    loggedEvent
                }
            }
    }
}
