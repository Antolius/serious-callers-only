package org.github.jantolis.seriouscallersonly.dsl

import java.time.Instant

data class Conversation(
        val channel: Channel,
        val user: User,
        var thread: Thread? = null,
        val messages: MutableList<PostedMessage> = mutableListOf()
)

data class User(
        val id: String
)

data class Channel(
        val id: String
)

data class Thread(
        val id: String
)

data class PostedMessage(
        val blocks: List<MessageBlock>,
        val author: User,
        val channel: Channel,
        val timestamp: Instant
)

data class Command(val command: String)

data class InvokedCommand(
        val text: String,
        val command: Command,
        val author: User,
        val channel: Channel,
        val timestamp: Instant
)