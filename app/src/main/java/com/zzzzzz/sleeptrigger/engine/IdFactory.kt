package com.zzzzzz.sleeptrigger.engine

fun interface IdFactory {
    fun newId(prefix: String): String
}

class TimestampIdFactory(private val clock: Clock = SystemClock) : IdFactory {
    override fun newId(prefix: String): String = "$prefix-${clock.nowMillis()}"
}

