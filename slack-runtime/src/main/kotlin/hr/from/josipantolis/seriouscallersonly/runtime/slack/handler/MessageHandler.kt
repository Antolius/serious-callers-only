package hr.from.josipantolis.seriouscallersonly.runtime.slack.handler

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.bolt.handler.BoltEventHandler
import com.slack.api.bolt.response.Response
import com.slack.api.model.event.MessageEvent
import hr.from.josipantolis.seriouscallersonly.api.*
import hr.from.josipantolis.seriouscallersonly.runtime.repo.Repo
import hr.from.josipantolis.seriouscallersonly.runtime.slack.AsyncSlackClient
import hr.from.josipantolis.seriouscallersonly.runtime.slack.Conversation
import hr.from.josipantolis.seriouscallersonly.runtime.slack.ConversationKey
import hr.from.josipantolis.seriouscallersonly.runtime.slack.MessageSender
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Clock

class MessageHandler(
    private val bot: Bot,
    private val clock: Clock,
    private val conversations: Repo<ConversationKey, Conversation>
) : BoltEventHandler<MessageEvent> {
    override fun apply(req: EventsApiPayload<MessageEvent>, ctx: EventContext): Response {
        GlobalScope.launch {
            handle(req, ctx)
        }
        return ctx.ack()
    }

    private suspend fun handle(req: EventsApiPayload<MessageEvent>, ctx: EventContext) {
        if (req.event.subtype != null) return
        val author = User(req.event.user)
        val content = MessageText(req.event.text)
        val thread = Thread(req.event.threadTs ?: req.event.ts)
        val channel = Channel(req.event.channel)
        val conversation = conversations
            .find(ConversationKey(channel, thread.id))
            ?: Conversation(
                channel = channel,
                thread = thread
            )
        val replier = (when {
            isResponseToAThread(req) -> {
                responseMsgReplier(channel, author, content, conversation)
            }
            isInstantMessage(req) -> {
                instantMsgReplier(channel, author, content)
            }
            else -> {
                publicMsgReplier(channel, author, content)
            }
        }) ?: return
        val reply = replier.cb()
        val client = AsyncSlackClient(ctx.asyncClient())
        MessageSender(conversations, client)
            .sendReply(reply, conversation)
    }

    private fun isResponseToAThread(req: EventsApiPayload<MessageEvent>) =
        req.event.threadTs != null

    private fun responseMsgReplier(
        channel: Channel,
        author: User,
        content: MessageText,
        conversation: Conversation
    ): Replier? {
        val replier = conversation.responseReplier
        conversation.responseReplier = null
        return replier
            ?.curry(
                Event.Interaction.UserResponded(
                    channel = channel,
                    happenedAt = clock.instant(),
                    author = author,
                    content = content
                )
            )
    }

    private fun isInstantMessage(req: EventsApiPayload<MessageEvent>) =
        req.event.channelType == "im"

    private fun instantMsgReplier(channel: Channel, author: User, content: MessageText) =
        bot.onPrivateMessage
            ?.curry(
                Event.PrivateMessageReceived(
                    channel = channel,
                    happenedAt = clock.instant(),
                    author = author,
                    content = content
                )
            )

    private fun publicMsgReplier(channel: Channel, author: User, content: MessageText) =
        bot.channelProtocols[channel]
            ?.onPublicMessage
            ?.curry(
                Event.PublicMessagePosted(
                    channel = channel,
                    happenedAt = clock.instant(),
                    author = author,
                    content = content
                )
            )

}