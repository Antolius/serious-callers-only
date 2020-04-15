package org.github.jantolis.seriouscallersonly.bot

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
    lateinit var ctx: Conversation
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
        var messageTsToDelete: String? = null
) {
    var key: ConversationKey? = null

    private val interactions = mutableMapOf<InteractionKey, LiveInteraction>()

    fun register(interaction: LiveInteraction) {
        interaction.ctx = this
        interactions[interaction.key] = interaction
    }

    fun find(key: InteractionKey) = interactions[key]

    fun clearInteractions() {
        interactions.clear()
    }
}