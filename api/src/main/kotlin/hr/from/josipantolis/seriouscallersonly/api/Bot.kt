package hr.from.josipantolis.seriouscallersonly.api

import hr.from.josipantolis.seriouscallersonly.api.EventReplier.*

data class TimerProtocol(
    val cron: String,
    val onTimer: TimerReplier
)

data class ChannelProtocol(
    val channel: Channel,
    val onUserJoined: UserJoinedChannelReplier? = null,
    val onPublicMessage: PublicMessagePostedReplier? = null,
    val timerProtocol: TimerProtocol? = null
)

data class CommandProtocol(
    val command: Command,
    val onCommandInvoked: CommandInvokedReplier
)

data class Bot(
    val channelProtocols: Map<Channel, ChannelProtocol> = mapOf(),
    val commandProtocols: Map<Command, CommandProtocol> = mapOf(),
    val onPrivateMessage: PrivateMessageReceivedReplier? = null,
    val onHomeTabVisit: HomeTabVisitedReplier? = null,
    val onBotJoinChannel: BotJoinedChannelReplier? = null
)