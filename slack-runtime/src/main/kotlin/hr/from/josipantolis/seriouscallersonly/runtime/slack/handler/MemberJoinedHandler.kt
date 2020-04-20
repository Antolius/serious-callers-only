package hr.from.josipantolis.seriouscallersonly.runtime.slack.handler

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.bolt.handler.BoltEventHandler
import com.slack.api.bolt.response.Response
import com.slack.api.model.event.MemberJoinedChannelEvent
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

class MemberJoinedHandler(
    private val bot: Bot,
    private val clock: Clock,
    private val conversations: Repo<ConversationKey, Conversation>
) : BoltEventHandler<MemberJoinedChannelEvent> {
    override fun apply(req: EventsApiPayload<MemberJoinedChannelEvent>, ctx: EventContext): Response {
        GlobalScope.launch {
            handle(req, ctx)
        }
        return ctx.ack()
    }

    private suspend fun handle(req: EventsApiPayload<MemberJoinedChannelEvent>, ctx: EventContext) {
        val user = User(req.event.user)
        val channel = Channel(req.event.channel)
        val replier = if (user.id == ctx.botUserId) {
            botJoinedReplier(channel)
        } else {
            userJoinerReplier(channel, user)
        } ?: return
        val convCtx = Conversation(
            channel = channel
        )
        val botReply = replier.cb()
        val client =
            AsyncSlackClient(ctx.asyncClient())
        MessageSender(conversations, client)
            .sendReply(botReply, convCtx)
    }

    private fun botJoinedReplier(channel: Channel): Replier? = bot
        .onBotJoinChannel
        ?.curry(
            Event.BotJoinedChannel(
                channel = channel,
                happenedAt = Instant.now(clock)
            )
        )

    private fun userJoinerReplier(channel: Channel, user: User): Replier? = bot
        .channelProtocols[channel]
        ?.onUserJoined
        ?.curry(
            Event.UserJoinedChannel(
                channel = channel,
                happenedAt = Instant.now(clock),
                user = user
            )
        )
}