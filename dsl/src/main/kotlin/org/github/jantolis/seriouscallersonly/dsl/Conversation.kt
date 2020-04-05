package org.github.jantolis.seriouscallersonly.dsl

import java.time.Instant
import java.time.ZoneId
import java.util.*

data class Channel(val id: String)

data class Thread(val id: String)

data class User(val id: String)

data class Moment(
        val instant: Instant,
        val localZone: ZoneId?
)

data class HistoryMessage(
        val id: String,
        val author: User,
        val postedAt: Moment,
        val text: String
) {
    val thread: Thread
        get() = Thread(this.id)
}

data class Conversation(
        var id: String = UUID.randomUUID().toString(),
        val channel: Channel,
        val thread: Thread? = null,
        val history: List<HistoryMessage> = listOf()
) {
    fun append(msg: HistoryMessage) = Conversation(id, channel, thread, history + msg)

    fun user() = history.map { it.author }.first()
}

interface Conversational {
    fun conversation(): Conversation
}
