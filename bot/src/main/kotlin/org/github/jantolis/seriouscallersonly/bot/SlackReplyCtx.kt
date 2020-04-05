package org.github.jantolis.seriouscallersonly.bot

import com.slack.api.methods.MethodsClient
import com.slack.api.model.ConversationType
import org.github.jantolis.seriouscallersonly.dsl.Content
import org.github.jantolis.seriouscallersonly.dsl.Conversation
import org.github.jantolis.seriouscallersonly.dsl.Destination
import org.github.jantolis.seriouscallersonly.dsl.ReplyCtx
import kotlin.reflect.KClass

class SlackReplyCtx(
        private val client: MethodsClient,
        private val user: String,
        private val channel: String,
        private val conversation: Conversation,
        private val replierRegisterer: (replier: ActionReplier) -> Unit
) : ReplyCtx {

    override var postTo = Destination.CONTINUE

    override fun <T : Content> reply(type: KClass<T>, init: T.() -> Unit) {
        when (type) {
            Content.Modal::class -> throw NotImplementedError()
            Content.Message::class -> {
                val msg = Content.Message()
                init(msg as T)
                client.chatPostMessage { builder ->
                    builder.channel(channelToPostTo())
                    builder.threadTs(threadToPostTo())
                    builder.blocks(msg.toBlocks(replierRegisterer))
                    builder
                }
            }
        }
    }

    override fun conversation() = conversation

    private fun channelToPostTo() = when (postTo) {
        Destination.PUBLIC -> channel
        Destination.THREAD -> channel
        Destination.IM -> imChannel()
        Destination.CONTINUE -> if (conversation.thread != null) { channel } else { imChannel() }
    }

    private fun threadToPostTo() = when(postTo) {
        Destination.PUBLIC -> null
        Destination.THREAD -> conversation.thread?.id
        Destination.IM -> null
        Destination.CONTINUE -> conversation.thread?.id
    }

    private fun imChannel() = client.usersConversations {
        it.types(listOf(ConversationType.IM)).limit(1).user(user)
    }.channels.firstOrNull()?.id ?: user
}