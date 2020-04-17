package hr.from.josipantolis.seriouscallersonly.runtime.slack

import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.model.event.MemberJoinedChannelEvent
import hr.from.josipantolis.seriouscallersonly.api.*
import hr.from.josipantolis.seriouscallersonly.runtime.slack.repository.ConcurrentRepo
import hr.from.josipantolis.seriouscallersonly.runtime.slack.repository.MapRepo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant

val conversations = ConcurrentRepo<ConversationKey?, Conversation>(MapRepo { it.key })

fun slackApp(
    bot: Bot,
    config: AppConfig = AppConfig(),
    clock: Clock = Clock.systemUTC()
) = App(config).apply {

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
                MessageSender(conversations, AsyncSlackClient(ctx.asyncClient()))
                    .sendReply(botReply, convCtx)
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
                        MessageSender(conversations, AsyncSlackClient(ctx.asyncClient()))
                            .sendReply(botReply, convCtx)
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
                    MessageSender(conversations, AsyncSlackClient(ctx.asyncClient()))
                        .sendErrors(errors, convCtx)
                } else {
                    val botReply = interaction.replier.replier(inter)
                    MessageSender(conversations, AsyncSlackClient(ctx.asyncClient()))
                        .sendReply(botReply, convCtx)
                }
            } finally {
                interaction.conversation.triggerId = null
            }
        }
        ctx.ack()
    }

}

fun <T> Replier<T>.curry(t: T) = VoidReplier { this.replier(t) }