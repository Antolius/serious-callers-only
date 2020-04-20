package hr.from.josipantolis.seriouscallersonly.runtime.slack.handler

import com.slack.api.bolt.App
import com.slack.api.methods.AsyncMethodsClient
import hr.from.josipantolis.seriouscallersonly.api.*
import hr.from.josipantolis.seriouscallersonly.runtime.slack.Conversation
import hr.from.josipantolis.seriouscallersonly.runtime.slack.Scheduler
import hr.from.josipantolis.seriouscallersonly.runtime.slack.mapToPublicMessage
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import java.time.Clock
import java.time.Instant

fun App.timer(scheduler: Scheduler, bot: Bot, handler: TimerHandler) {
    bot.channelProtocols
        .filter { (_, protocol) -> protocol.timerProtocol != null }
        .map { (channel, protocol) -> channel to protocol.timerProtocol }
        .forEach { (channel, protocol) ->
            scheduler.schedule(
                protocol!!.cron,
                Runnable { handler.execute(channel, protocol) }
            )
        }
}

class TimerHandler(
    private val app: App,
    private val clock: Clock
) {
    private val slackMethods: AsyncMethodsClient
        get() = app.config().slack.methodsAsync(app.config().singleTeamBotToken)

    fun execute(channel: Channel, protocol: TimerProtocol) {
        runBlocking {
            try {
                handle(clock.instant(), channel, protocol.onTimer)
            } catch (e: Exception) {
                TODO("log")
            }
        }
    }

    private suspend fun handle(instant: Instant, channel: Channel, replier: EventReplier.TimerReplier) {
        val conversation = Conversation(channel = channel)
        val event = Event.Timer(
            channel = channel,
            happenedAt = instant
        )
        val reply = replier.cb(event)
        if (reply is Reply.Message) {
            val req = conversation.mapToPublicMessage(reply)
            val res = slackMethods.chatPostMessage(req).await()
            if (!res.isOk) {
                throw Exception(res.error)
            }
        }
    }

}