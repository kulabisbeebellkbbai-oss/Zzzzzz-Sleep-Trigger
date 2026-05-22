package com.zzzzzz.sleeptrigger.store

import android.content.Context
import com.zzzzzz.sleeptrigger.engine.LoggedTaskEvent
import com.zzzzzz.sleeptrigger.engine.TaskRun
import com.zzzzzz.sleeptrigger.engine.TaskRunStatus
import com.zzzzzz.sleeptrigger.engine.TaskType
import com.zzzzzz.sleeptrigger.engine.TriggerEvent
import com.zzzzzz.sleeptrigger.engine.TriggerSource
import com.zzzzzz.sleeptrigger.engine.TriggerType
import org.json.JSONArray
import org.json.JSONObject

class EventLogStore(context: Context) : TaskEventRepository {
    private val preferences = context.getSharedPreferences("event-log", Context.MODE_PRIVATE)

    override fun append(triggerEvent: TriggerEvent, taskRun: TaskRun) {
        val entries = readJsonArray()
        entries.put(
            JSONObject()
                .put("triggerEvent", triggerEvent.toJson())
                .put("taskRun", taskRun.toJson())
        )
        writeJsonArray(entries)
    }

    override fun updateTaskRun(taskRun: TaskRun) {
        val entries = readJsonArray()
        for (index in 0 until entries.length()) {
            val entry = entries.getJSONObject(index)
            val existingTask = entry.getJSONObject("taskRun")
            if (existingTask.getString("id") == taskRun.id) {
                entry.put("taskRun", taskRun.toJson())
                writeJsonArray(entries)
                return
            }
        }
    }

    override fun findTaskRun(id: String): TaskRun? {
        return readAll().firstOrNull { it.taskRun.id == id }?.taskRun
    }

    override fun readAll(): List<LoggedTaskEvent> {
        val entries = readJsonArray()
        return (0 until entries.length()).map { index ->
            val entry = entries.getJSONObject(index)
            LoggedTaskEvent(
                triggerEvent = entry.getJSONObject("triggerEvent").toTriggerEvent(),
                taskRun = entry.getJSONObject("taskRun").toTaskRun()
            )
        }
    }

    private fun readJsonArray(): JSONArray {
        return JSONArray(preferences.getString(KEY_ENTRIES, "[]"))
    }

    private fun writeJsonArray(entries: JSONArray) {
        preferences.edit().putString(KEY_ENTRIES, entries.toString()).apply()
    }

    private fun TriggerEvent.toJson(): JSONObject {
        return JSONObject()
            .put("id", id)
            .put("triggerType", triggerType.name)
            .put("source", source.name)
            .put("detectedAtMillis", detectedAtMillis)
            .put("confidence", confidence.toDouble())
            .put("metadata", metadata.toJson())
    }

    private fun TaskRun.toJson(): JSONObject {
        return JSONObject()
            .put("id", id)
            .put("triggerEventId", triggerEventId)
            .put("taskType", taskType.name)
            .put("scheduledForMillis", scheduledForMillis)
            .put("startedAtMillis", startedAtMillis)
            .put("completedAtMillis", completedAtMillis)
            .put("status", status.name)
            .put("resultMessage", resultMessage)
    }

    private fun JSONObject.toTriggerEvent(): TriggerEvent {
        return TriggerEvent(
            id = getString("id"),
            triggerType = TriggerType.valueOf(getString("triggerType")),
            source = TriggerSource.valueOf(getString("source")),
            detectedAtMillis = getLong("detectedAtMillis"),
            confidence = getDouble("confidence").toFloat(),
            metadata = optJSONObject("metadata")?.toStringMap().orEmpty()
        )
    }

    private fun JSONObject.toTaskRun(): TaskRun {
        return TaskRun(
            id = getString("id"),
            triggerEventId = getString("triggerEventId"),
            taskType = TaskType.valueOf(getString("taskType")),
            scheduledForMillis = getLong("scheduledForMillis"),
            startedAtMillis = nullableLong("startedAtMillis"),
            completedAtMillis = nullableLong("completedAtMillis"),
            status = TaskRunStatus.valueOf(getString("status")),
            resultMessage = nullableString("resultMessage")
        )
    }

    private fun JSONObject.nullableLong(key: String): Long? {
        return if (isNull(key)) null else getLong(key)
    }

    private fun JSONObject.nullableString(key: String): String? {
        return if (isNull(key)) null else getString(key)
    }

    private fun Map<String, String>.toJson(): JSONObject {
        val json = JSONObject()
        forEach { (key, value) -> json.put(key, value) }
        return json
    }

    private fun JSONObject.toStringMap(): Map<String, String> {
        return keys().asSequence().associateWith { key -> getString(key) }
    }

    private companion object {
        const val KEY_ENTRIES = "entries"
    }
}
