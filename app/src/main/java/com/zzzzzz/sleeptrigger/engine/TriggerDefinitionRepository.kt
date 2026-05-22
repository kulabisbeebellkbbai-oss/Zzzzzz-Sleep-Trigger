package com.zzzzzz.sleeptrigger.engine

interface TriggerDefinitionRepository {
    fun readDefinitions(): List<TriggerDefinition>
    fun saveDefinition(definition: TriggerDefinition)
}

class InMemoryTriggerDefinitionRepository(
    definitions: List<TriggerDefinition> = DefaultTriggerDefinitions.all
) : TriggerDefinitionRepository {
    private val definitionsById = definitions.associateBy { it.id }.toMutableMap()

    override fun readDefinitions(): List<TriggerDefinition> {
        return definitionsById.values.sortedBy { it.id }
    }

    override fun saveDefinition(definition: TriggerDefinition) {
        definitionsById[definition.id] = definition
    }
}

object DefaultTriggerDefinitions {
    val sleepPauseMedia = TriggerDefinition(
        id = "sleep-pause-media",
        triggerType = TriggerType.ASLEEP_DETECTED,
        enabled = true,
        delayMillis = 5 * 60 * 1000,
        taskType = TaskType.PAUSE_MEDIA
    )

    val stoodUpAfterWake = TriggerDefinition(
        id = "stood-up-after-wake",
        triggerType = TriggerType.STOOD_UP_AFTER_WAKE,
        enabled = true,
        delayMillis = 0,
        taskType = TaskType.PAUSE_MEDIA
    )

    val all: List<TriggerDefinition> = listOf(sleepPauseMedia, stoodUpAfterWake)
}

