package org.github.jantolis.seriouscallersonly.bot

import com.slack.api.bolt.App
import com.slack.api.bolt.context.Context
import com.slack.api.model.event.MemberJoinedChannelEvent
import com.slack.api.model.event.MessageEvent
import org.github.jantolis.seriouscallersonly.dsl.*
import java.time.Instant
import java.time.ZoneId


fun bot(protocols: Map<String, Protocol>) = App().apply {
    val store = Store()

    event(MemberJoinedChannelEvent::class.java) { payload, ctx ->
        val user = payload.event.user
        if (user != ctx.botUserId) {
            val channel = payload.event.channel
            val protocol = protocols[channel]
            if (protocol != null) {
                val conv = Conversation(channel = Channel(channel))
                val replyCtx = SlackReplyCtx(ctx.client(), user, channel, conv, store.register(conv))
                protocol.userJoinCallback(replyCtx, User(user))
            }
        }
        ctx.ack()
    }

    event(MessageEvent::class.java) { payload, ctx ->
        val msgEvent = payload.event
        if (msgEvent.subtype == null) {
            val channel = payload.event.channel
            val user = payload.event.user
            val thread = payload.event.ts
            val isMsgOutsideThread = msgEvent.subtype == null && msgEvent.threadTs == null
            if (isMsgOutsideThread) {
                val protocol = protocols[channel]
                if (protocol != null) {
                    val now = Moment(Instant.now(), ctx.userTimeZone(user))
                    val msg = HistoryMessage(msgEvent.clientMsgId, User(user), now, msgEvent.text)
                    val conv = Conversation(channel = Channel(channel), thread = Thread(thread)).append(msg)
                    val replyCtx = SlackReplyCtx(ctx.client(), user, channel, conv, store.register(conv))
                    protocol.newMessageCallback(replyCtx, msg)
                }
            }
        }
        ctx.ack()
    }

    blockAction(".*".toPattern()) { req, ctx ->
        val action = req.payload.actions.firstOrNull()
        if (action != null) {
            val savedState = store.find(action.actionId)
            if (savedState != null) {
                val (conv, replies) = savedState
                val reply = replies[action.selectedOption?.value]
                if (reply != null) {
                    val user = conv.user()
                    val channel = conv.channel
                    val now = Moment(Instant.now(), ctx.userTimeZone(user.id))
                    val msg = HistoryMessage(action.actionId, user, now, action.text?.text ?: action.selectedOption?.value ?: "answer")
                    val newConv = conv.append(msg)
                    val replyCtx = SlackReplyCtx(ctx.client(), user.id, channel.id, newConv, store.register(newConv))
                    replyCtx.reply()
                    store.unregister(action.actionId)
                }
            }
        }
        ctx.ack()
    }
}

fun Context.userTimeZone(user: String): ZoneId? {
    val tz = this.client().usersInfo { it.user(user) }.user?.tzLabel
    return try {
        ZoneId.of(tz)
    } catch (_: Exception) {
        null
    }
}

