package hr.from.josipantolis.seriouscallersonly.app

import hr.from.josipantolis.seriouscallersonly.api.Bot
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation

@Endpoint(id = "bot")
open class BotActuatorEndpoint(private val bot: Bot) {

    @ReadOperation
    fun bot() = mapOf(
        "channelProtocols" to bot.channelProtocols
            .mapValues {
                mapOf(
                    "onUserJoined" to (it.value.onUserJoined != null),
                    "onPublicMessage" to (it.value.onPublicMessage != null),
                    "timerProtocol" to (it.value.timerProtocol?.cron)
                )
            },
        "commandProtocols" to bot.commandProtocols.keys.map { it.cmd },
        "onBotJoinChannel" to (bot.onBotJoinChannel != null),
        "onHomeTabVisit" to (bot.onHomeTabVisit != null),
        "onPrivateMessage" to (bot.onPrivateMessage != null)
    )

}