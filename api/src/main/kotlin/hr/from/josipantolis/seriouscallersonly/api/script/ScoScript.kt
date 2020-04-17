package hr.from.josipantolis.seriouscallersonly.api.script

import hr.from.josipantolis.seriouscallersonly.api.ChannelProtocol
import hr.from.josipantolis.seriouscallersonly.api.CommandProtocol
import hr.from.josipantolis.seriouscallersonly.api.EventReplier.*
import java.util.*
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "SCO Script",
    fileExtension = ScoScriptCtx.FILE_EXTENSION,
    compilationConfiguration = ScoCompilationConfig::class
)
abstract class ScoScript(private val ctx: ScoScriptCtx) : ScoScriptCtx by ctx

interface ScoScriptCtx {
    companion object {
        const val FILE_EXTENSION = "call.kts"
    }

    val env: Properties
    fun register(channelProtocol: ChannelProtocol)
    fun register(commandProtocol: CommandProtocol)
    fun register(privateMessageReplier: PrivateMessageReceivedReplier)
    fun register(homeTabVisitReplier: HomeTabVisitedReplier)
    fun register(botJoinedChannelReplier: BotJoinedChannelReplier)
}
