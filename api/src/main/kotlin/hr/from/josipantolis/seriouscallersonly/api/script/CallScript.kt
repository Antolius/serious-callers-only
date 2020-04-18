package hr.from.josipantolis.seriouscallersonly.api.script

import hr.from.josipantolis.seriouscallersonly.api.ChannelProtocol
import hr.from.josipantolis.seriouscallersonly.api.CommandProtocol
import hr.from.josipantolis.seriouscallersonly.api.EventReplier.*
import hr.from.josipantolis.seriouscallersonly.api.dsl.CallScriptExtensions
import hr.from.josipantolis.seriouscallersonly.api.dsl.CallScriptExtensionsRoot
import java.util.*
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "Call Script",
    fileExtension = CallScriptCtx.FILE_EXTENSION,
    compilationConfiguration = CallScriptCompileConfig::class
)
abstract class CallScript(
    private val ctx: CallScriptCtx,
    private val ext: CallScriptExtensions
) : CallScriptCtx by ctx, CallScriptExtensions by ext

interface CallScriptCtx {
    companion object {
        const val FILE_EXTENSION = "call.kts"
    }

    val env: Properties
    infix fun register(channelProtocol: ChannelProtocol)
    infix fun register(commandProtocol: CommandProtocol)
    infix fun register(privateMessageReplier: PrivateMessageReceivedReplier)
    infix fun register(homeTabVisitReplier: HomeTabVisitedReplier)
    infix fun register(botJoinedChannelReplier: BotJoinedChannelReplier)
}
