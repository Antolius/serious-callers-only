package org.github.jantolis.seriouscallersonly.bot

import org.github.jantolis.seriouscallersonly.dsl.*


data class InteractionKey<T>(
        val blockId: String,
        val elementId: String,
        val value: String
)

class LiveInteraction<T>(
        val key: InteractionKey<T>,
        val validator: Validator<String> = Validator { listOf() },
        val replier: VoidReplier,
        val replacer: Replacer<T>?
) {
    lateinit var ctx: ConversationContext
}

class ConversationContext(
        val conversation: Conversation,
        val interactions: MutableMap<String, MutableMap<String, MutableMap<String, LiveInteraction<*>>>>,
        var triggerId: String? = null,
        var lastMessageId: String? = null
) {
    fun <T> register(interaction: LiveInteraction<T>) {
        interaction.ctx = this
        val key = interaction.key
        interactions.computeIfAbsent(key.blockId) { mutableMapOf() }
                .computeIfAbsent(key.elementId) { mutableMapOf() }
                .put(key.value, interaction)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> find(key: InteractionKey<T>): LiveInteraction<T>? {
        return interactions[key.blockId]
                ?.get(key.elementId)
                ?.get(key.value) as LiveInteraction<T>
    }

    fun unregisterBlock(blockId: String) {
        interactions.remove(blockId)
    }
}