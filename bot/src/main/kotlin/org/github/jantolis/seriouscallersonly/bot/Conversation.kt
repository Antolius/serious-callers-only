package org.github.jantolis.seriouscallersonly.bot

import org.github.jantolis.seriouscallersonly.bot.repository.ConcurrentRepo
import org.github.jantolis.seriouscallersonly.bot.repository.MapRepo
import org.github.jantolis.seriouscallersonly.bot.repository.Repo
import org.github.jantolis.seriouscallersonly.dsl.*

data class InteractionKey(
        val elementId: String,
        val value: String
)

class LiveInteraction(
        val key: InteractionKey,
        val validator: Validator? = null,
        val replier: Replier<Interaction>
) {
    lateinit var conversation: Conversation
}

data class ConversationKey(
        val channel: Channel,
        val messageTs: String
)

class Conversation(
        val user: User,
        val channel: Channel,
        val thread: Thread? = null,
        var triggerId: String? = null,
        var updateableMessageTs: String? = null,
        var messageTsToDelete: String? = null,
        val interactionsRepo: Repo<InteractionKey, LiveInteraction> = ConcurrentRepo(MapRepo { it.key })
) : Repo<InteractionKey, LiveInteraction> by interactionsRepo {
    lateinit var key: ConversationKey

    override suspend fun store(element: LiveInteraction) {
        element.conversation = this
        interactionsRepo.store(element)
    }
}