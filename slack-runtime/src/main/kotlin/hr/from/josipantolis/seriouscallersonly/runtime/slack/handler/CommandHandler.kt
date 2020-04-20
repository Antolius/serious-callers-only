package hr.from.josipantolis.seriouscallersonly.runtime.slack.handler

import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.handler.builtin.SlashCommandHandler
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import com.slack.api.bolt.response.Response
import hr.from.josipantolis.seriouscallersonly.api.*
import hr.from.josipantolis.seriouscallersonly.runtime.repo.Repo
import hr.from.josipantolis.seriouscallersonly.runtime.slack.AsyncSlackClient
import hr.from.josipantolis.seriouscallersonly.runtime.slack.Conversation
import hr.from.josipantolis.seriouscallersonly.runtime.slack.ConversationKey
import hr.from.josipantolis.seriouscallersonly.runtime.slack.MessageSender
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant

class CommandHandler(
    private val bot: Bot,
    private val clock: Clock,
    private val conversations: Repo<ConversationKey, Conversation>
) : SlashCommandHandler {
    override fun apply(req: SlashCommandRequest, ctx: SlashCommandContext): Response {
        GlobalScope.launch {
            handle(req, ctx)
        }
        return ctx.ack()
    }

    private suspend fun handle(req: SlashCommandRequest, ctx: SlashCommandContext) {
        val command = Command(req.payload.command)
        val author = User(req.payload.userId)
        val channel = Channel(req.payload.channelId)
        val event = Event.CommandInvoked(
            channel = channel,
            happenedAt = Instant.now(clock),
            command = command,
            invoker = author,
            invocationText = req.payload.text ?: ""
        )
        val replier = bot.commandProtocols[command]
            ?.onCommandInvoked
            ?: return
        val conversation = Conversation(
            channel = channel,
            triggerId = req.payload.triggerId
        )
        try {
            val reply = replier.cb(event)
            MessageSender(
                conversations,
                AsyncSlackClient(ctx.asyncClient())
            )
                .sendReply(reply, conversation)
        } finally {
            ctx.triggerId = null
        }
    }
}