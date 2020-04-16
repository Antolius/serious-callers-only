package hr.from.josipantolis.seriouscallersonly.dsl

import java.time.Instant

data class User(
        val id: String
) {
    val mention: String
        get() = "<@$id>"

    object specialMention {
        val here = "<!here|here>"
        val channel = "<!channel>"
        val everyone = "<!everyone>"
    }
}

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

data class CommandInvocation(
        val text: String?,
        val command: Command,
        val invoker: User,
        val channel: Channel,
        val timestamp: Instant
)

data class Interaction(
        val value: String,
        val actor: User,
        val channel: Channel,
        val timestamp: Instant
)