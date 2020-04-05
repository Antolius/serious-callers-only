package org.github.jantolis.seriouscallersonly.bot

import org.github.jantolis.seriouscallersonly.dsl.Conversation
import org.github.jantolis.seriouscallersonly.dsl.Reply

data class ActionReplier(val actionId: String, val replies: MutableMap<String, Reply> = mutableMapOf())

class Store {

    private val conversations = mutableMapOf<String, Pair<Conversation, MutableMap<String, Reply>>>()

    @Synchronized
    fun register(conv: Conversation): (ActionReplier) -> Unit {
        return { conversations[it.actionId] = Pair(conv, it.replies) }
    }

    @Synchronized
    fun unregister(actionId: String) {
        conversations.remove(actionId)
    }

    @Synchronized
    fun find(actionId: String): Pair<Conversation, MutableMap<String, Reply>>? {
        return conversations[actionId]
    }
}