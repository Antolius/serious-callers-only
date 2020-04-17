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
                    "onNewChannelMessage" to (it.value.onNewChannelMessage != null),
                    "onUserJoinChannel" to (it.value.onUserJoinChannel != null)
                )
            },
        "commandProtocols" to bot.commandProtocols.keys.map { it.command },
        "onBotJoinChannel" to (bot.onBotJoinChannel != null),
        "onNewPrivateMessage" to (bot.onNewPrivateMessage != null),
        "onUserVisitHomeTab" to (bot.onUserVisitHomeTab != null)
    )

}