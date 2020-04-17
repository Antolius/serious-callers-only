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
            bot.onBotJoinChannel?.curry(Event.BotJoinedChannel(channel, Instant.now(clock)))
        } else {
            bot.channelProtocols[channel]?.onUserJoined?.curry(
                Event.UserJoinedChannel(
                    channel,
                    Instant.now(clock),
                    user
                )
            )
        }?.apply {
            GlobalScope.launch {
                val convCtx = Conversation(user = user, channel = channel)
                val botReply = cb()
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
        val invocation = Event.CommandInvoked(
            channel = channel,
            happenedAt = Instant.now(clock),
            command = command,
            invoker = author,
            invocationText = req.payload.text ?: ""
        )
        bot.commandProtocols[command]
            ?.onCommandInvoked
            ?.curry(invocation)
            ?.apply {
                GlobalScope.launch {
                    val convCtx = Conversation(
                        channel = channel,
                        user = author,
                        triggerId = req.payload.triggerId
                    )
                    try {
                        val botReply = cb()
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
            try {
                val event = req.mapToEvent(clock) ?: return@launch
                val convKey = req.mapToConversationKey()
                val conv = conversations.find(convKey) ?: return@launch
                val interKey = req.mapToInteractionKey() ?: return@launch
                if (conv.messageTsToDelete != null) {
                    val delRes = ctx.client().chatDelete {
                        it.channel(conv.channel.id)
                        it.ts(conv.messageTsToDelete)
                    }
                    conv.messageTsToDelete = null
                    println(delRes)
                }
                conv.triggerId = req.payload.triggerId
                try {
                    val errors = conv.validate(event, interKey)
                    if (errors != null && errors.isNotEmpty()) {
                        MessageSender(conversations, AsyncSlackClient(ctx.asyncClient()))
                            .sendErrors(errors, conv)
                        return@launch
                    }
                    val reply = conv.renderReply(event, interKey)
                    if (reply != null) {
                        MessageSender(conversations, AsyncSlackClient(ctx.asyncClient()))
                            .sendReply(reply, conv)
                    }
                } finally {
                    conv.triggerId = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        ctx.ack()
    }

}
