package com.zzzzzz.sleeptrigger.engine

interface TaskScheduler {
    fun schedule(request: ScheduledTaskRequest)
    fun cancel(taskRunId: String)
}

