package hr.from.josipantolis.seriouscallersonly.runtime.slack

import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.model.event.MemberJoinedChannelEvent
import com.slack.api.model.event.MessageEvent
import hr.from.josipantolis.seriouscallersonly.api.Bot
import hr.from.josipantolis.seriouscallersonly.runtime.repo.ConcurrentRepo
import hr.from.josipantolis.seriouscallersonly.runtime.repo.MapRepo
import hr.from.josipantolis.seriouscallersonly.runtime.repo.Repo
import hr.from.josipantolis.seriouscallersonly.runtime.slack.handler.*
import java.time.Clock

fun conversationsRepo() = ConcurrentRepo<ConversationKey?, Conversation>(MapRepo { it.key })

fun slackApp(
    bot: Bot,
    scheduler: Scheduler,
    conversations: Repo<ConversationKey, Conversation>,
    config: AppConfig = AppConfig(),
    clock: Clock = Clock.systemUTC()
) = App(config).apply {
    timer(scheduler, bot, TimerHandler(this, clock))
    event(
        MemberJoinedChannelEvent::class.java,
        MemberJoinedHandler(bot, clock, conversations)
    )
    event(
        MessageEvent::class.java,
        MessageHandler(bot, clock, conversations)
    )
    command(
        ".*".toPattern(),
        CommandHandler(bot, clock, conversations)
    )
    blockAction(
        ".*".toPattern(),
        ActionHandler(clock, conversations)
    )

}
