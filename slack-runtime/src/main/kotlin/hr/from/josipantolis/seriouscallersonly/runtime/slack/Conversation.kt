package hr.from.josipantolis.seriouscallersonly.runtime.slack

import hr.from.josipantolis.seriouscallersonly.api.*
import hr.from.josipantolis.seriouscallersonly.runtime.slack.repository.ConcurrentRepo
import hr.from.josipantolis.seriouscallersonly.runtime.slack.repository.MapRepo
import hr.from.josipantolis.seriouscallersonly.runtime.slack.repository.Repo

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
    var key: ConversationKey? = null

    override suspend fun store(element: LiveInteraction) {
        element.conversation = this
        interactionsRepo.store(element)
    }
}