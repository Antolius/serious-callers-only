package org.github.jantolis.seriouscallersonly.bot

import com.slack.api.bolt.App
import com.slack.api.bolt.context.Context
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.event.MemberJoinedChannelEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.github.jantolis.seriouscallersonly.bot.repository.ConcurrentRepo
import org.github.jantolis.seriouscallersonly.dsl.*
import java.time.Clock
import java.time.Instant

val conversations = ConcurrentRepo<ConversationKey, Conversation>()

fun slackApp(bot: Bot, clock: Clock = Clock.systemUTC()) = App().apply {

    event(MemberJoinedChannelEvent::class.java) { payload, ctx ->
        val user = User(payload.event.user)
        val channel = Channel(payload.event.channel)
        if (user.id == ctx.botUserId) {
            bot.onBotJoinChannel?.curry(channel)
        } else {
            bot.channelProtocols[channel]?.onUserJoinChannel?.curry(user)
        }?.apply {
            GlobalScope.launch {
                val convCtx = Conversation(user = user, channel = channel)
                val botReply = replier(null)
                ctx.reply(botReply, convCtx)
            }
        }
        ctx.ack()
    }

    command(".*".toPattern()) { req, ctx ->
        val command = Command(req.payload.command)
        val author = User(req.payload.userId)
        val channel = Channel(req.payload.channelId)
        val invocation = CommandInvocation(
                text = req.payload.text,
                command = command,
                invoker = author,
                channel = channel,
                timestamp = Instant.now(clock)
        )
        bot.commandProtocols[command]
                ?.onSlashCommand
                ?.curry(invocation)
                ?.apply {
                    GlobalScope.launch {
                        val convCtx = Conversation(
                                channel = channel,
                                user = author,
                                triggerId = req.payload.triggerId
                        )
                        try {
                            val botReply = replier(null)
                            ctx.reply(botReply, convCtx)
                        } finally {
                            ctx.triggerId = null
                        }
                    }
                }
        ctx.ack()
    }

    blockAction(".*".toPattern()) { req, ctx ->
        GlobalScope.launch {
            val convKey = ConversationKey(
                    channel = Channel(req.payload.channel.id),
                    messageTs = req.payload.message.ts
            )
            val convCtx = conversations.find(convKey) ?: return@launch
            val action = req.payload.actions.first() ?: return@launch
            val value = if (action.selectedOption != null) {
                action.selectedOption.value
            } else {
                action.text.text
            }
            val interactionKey = InteractionKey(
                    elementId = action.actionId,
                    value = value
            )
            val interaction = convCtx.find(interactionKey) ?: return@launch
            interaction.conversation.triggerId = req.payload.triggerId
            if (convCtx.messageTsToDelete != null) {
                val delRes = ctx.client().chatDelete {
                    it.channel(convCtx.channel.id)
                    it.ts(convCtx.messageTsToDelete)
                }
                convCtx.messageTsToDelete = null
                println(delRes)
            }

            try {
                val validator = interaction.validator
                val inter = Interaction(
                        value = value,
                        channel = convCtx.channel,
                        actor = User(req.payload.user.id),
                        timestamp = Instant.now(clock)
                )
                val errors = validator?.validator?.invoke(inter) ?: listOf()
                if (errors.isNotEmpty()) {
                    val slackErrRes = ctx.client()
                            .chatPostMessage {
                                it.channel(convKey.channel.id)
                                val ts = convCtx.thread
                                if (ts != null) {
                                    it.threadTs(ts.id)
                                }
                                it.blocks(Blocks.asBlocks(SectionBlock.builder().text(PlainTextObject.builder().text(errors.joinToString(
                                        separator = "\n:warning: ",
                                        prefix = ":warning: "
                                )).build()).build()))
                            }
                    convCtx.messageTsToDelete = slackErrRes.ts
                } else {

                    val botReply = interaction.replier.replier(inter)
                    ctx.reply(botReply, interaction.conversation)
                }
            } finally {
                interaction.conversation.triggerId = null
            }
        }
        ctx.ack()
    }

}

fun <T> Replier<T>.curry(t: T) = VoidReplier { this.replier(t) }

suspend fun Context.reply(reply: Reply, ctx: Conversation) {
    val conversationKey = ctx.key
    if (conversationKey != null) {
        conversations.remove(conversationKey)
    }
    when (reply) {
        is Reply.Message -> {
            when (val visibleTo = reply.visibleTo) {
                is Visibility.Ephemeral -> {
                    val slackMsg = ctx.mapToEphemeralMessage(reply, visibleTo.user)
                    val slackResp = client().chatPostEphemeral(slackMsg)
                    if (!slackResp.isOk) {
                        throw Exception(slackResp.error)
                    }
                    ctx.key = ConversationKey(ctx.channel, slackResp.messageTs)
                    conversations.store(ctx.key!!, ctx)
                }
                Visibility.Public -> {
                    val slackMsg = ctx.mapToPublicMessage(reply)
                    val slackResp = client().chatPostMessage(slackMsg)
                    if (!slackResp.isOk) {
                        throw Exception(slackResp.error)
                    }
                    ctx.updateableMessageTs = slackResp.ts
                    ctx.key = ConversationKey(ctx.channel, slackResp.ts)
                    conversations.store(ctx.key!!, ctx)
                }
            }
            reply.andThen?.also {
                this.reply(it.replier(null), ctx)
            }
        }
        is Reply.ReplacementMessage -> {
            val messageTsoUpdate = ctx.updateableMessageTs
            if (messageTsoUpdate != null) {
                val slackMsg = ctx.mapToUpdateMessage(reply, messageTsoUpdate)
                val slackResp = client().chatUpdate(slackMsg)
                if (!slackResp.isOk) {
                    throw Exception(slackResp.error)
                }
                ctx.updateableMessageTs = slackResp.ts
                ctx.key = ConversationKey(ctx.channel, slackResp.ts)
                conversations.store(ctx.key!!, ctx)
                reply.andThen?.also {
                    this.reply(it.replier(null), ctx)
                }
            }
        }
        else -> {
        }
    }
}