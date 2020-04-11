package org.github.jantolis.seriouscallersonly.bot

import com.slack.api.bolt.App
import com.slack.api.bolt.context.Context
import com.slack.api.model.event.MemberJoinedChannelEvent
import com.slack.api.model.event.MessageEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.github.jantolis.seriouscallersonly.dsl.*
import java.time.Instant


val interactions = mutableMapOf<String, MutableMap<String, MutableMap<String, LiveInteraction<*>>>>()

fun slackApp(bot: Bot) = App().apply {

    event(MemberJoinedChannelEvent::class.java) { payload, ctx ->
        val user = User(payload.event.user)
        val channel = Channel(payload.event.channel)
        if (user.id == ctx.botUserId) {
            bot.onBotJoinChannel?.curry(channel)
        } else {
            bot.channelProtocols[channel]?.onUserJoinChannel?.curry(user)
        }?.apply {
            GlobalScope.launch {
                val conv = Conversation(channel = channel, user = user)
                val convCtx = ConversationContext(conversation = conv, interactions = interactions)
                val botReply = replier(null)
                ctx.reply(botReply, convCtx)
            }
        }
        ctx.ack()
    }

//    event(MessageEvent::class.java) { payload, ctx ->
//        val msgEvent = payload.event
//        if (msgEvent.subtype == null) {
//            val channel = payload.event.channel
//            val user = payload.event.user
//            val thread = payload.event.ts
//            val isMsgOutsideThread = msgEvent.subtype == null && msgEvent.threadTs == null
//            if (isMsgOutsideThread) {
//                val protocol = protocols[channel]
//                if (protocol != null) {
//                    val now = Moment(Instant.now(), ctx.userTimeZone(user))
//                    val msg = HistoryMessage(msgEvent.clientMsgId, User(user), now, msgEvent.text)
//                    val conv = Conversation(channel = Channel(channel), thread = Thread(thread)).append(msg)
//                    val replyCtx = SlackReplyCtx(ctx.client(), user, channel, conv, store.register(conv))
//                    protocol.newMessageCallback(replyCtx, msg)
//                }
//            }
//        }
//        ctx.ack()
//    }
//
//    blockAction(".*".toPattern()) { req, ctx ->
//        val action = req.payload.actions.firstOrNull()
//        if (action != null) {
//            val savedState = store.find(action.actionId)
//            if (savedState != null) {
//                val (conv, replies) = savedState
//                val reply = replies[action.selectedOption?.value]
//                if (reply != null) {
//                    val user = conv.user()
//                    val channel = conv.channel
//                    val now = Moment(Instant.now(), ctx.userTimeZone(user.id))
//                    val msg = HistoryMessage(action.actionId, user, now, action.text?.text
//                            ?: action.selectedOption?.value ?: "answer")
//                    val newConv = conv.append(msg)
//                    val replyCtx = SlackReplyCtx(ctx.client(), user.id, channel.id, newConv, store.register(newConv))
//                    replyCtx.reply()
//                    store.unregister(action.actionId)
//                }
//            }
//        }
//        ctx.ack()
//    }
}

fun <T> Replier<T>.curry(t: T) = VoidReplier { this.replier(t) }

suspend fun Context.reply(reply: Reply, ctx: ConversationContext) {
    when (reply) {
        is Reply.Message -> {
            val slackMsg = ctx.mapToSlackMessage(reply)
            client().chatPostMessage(slackMsg)
            reply.andThen?.also {
                this.reply(it.replier(null), ctx)
            }
        }
        else -> {
        }
    }
}