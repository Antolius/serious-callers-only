package hr.from.josipantolis.seriouscallersonly.runtime.script

import hr.from.josipantolis.seriouscallersonly.api.*
import hr.from.josipantolis.seriouscallersonly.api.dsl.CallScriptExtensionsRoot
import hr.from.josipantolis.seriouscallersonly.api.script.CallScript
import hr.from.josipantolis.seriouscallersonly.api.script.CallScriptCtx
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptDiagnostic.Severity.ERROR
import kotlin.script.experimental.api.ScriptDiagnostic.Severity.FATAL
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost


class Loader(
    private val env: Properties = Properties(),
    private val host: BasicJvmScriptingHost = BasicJvmScriptingHost()
) {
    fun load(scriptsDir: File) = scriptsDir.walkBottomUp()
        .onEnter { it.canRead() }
        .filter { it.isFile }
        .filter { it.name.endsWith(".${CallScriptCtx.FILE_EXTENSION}") }
        .map { it.readText(StandardCharsets.UTF_8) }
        .map { it.toScriptSource() }
        .fold(BotBuilder(env), this::runScript)
        .buildBot()

    private fun runScript(builder: BotBuilder, script: SourceCode): BotBuilder {
        val reports = host.evalWithTemplate<CallScript>(
            script = script,
            evaluation = {
                constructorArgs(builder, CallScriptExtensionsRoot())
            }
        ).reports
        checkForErrors(reports)
        return builder
    }

    private fun checkForErrors(reports: List<ScriptDiagnostic>) {
        val exception = reports.mapNotNull { it.exception }.firstOrNull()
        if (exception != null) {
            throw exception
        }
        val errors = reports
            .filter { setOf(ERROR, FATAL).contains(it.severity) }
            .joinToString(separator = "\n") {
                "@line=${it.location?.start?.line ?: "?"};clo=${it.location?.start?.col ?: "?"}: ${it.message}"
            }
        if (errors.isNotEmpty()) {
            throw Exception(errors)
        }
    }

}

private class BotBuilder(override val env: Properties) : CallScriptCtx {
    private val channelProtocols = mutableMapOf<Channel, ChannelProtocol>()
    private val commandProtocols = mutableMapOf<Command, CommandProtocol>()
    private var onPrivateMessage: EventReplier.PrivateMessageReceivedReplier? = null
    private var onHomeTabVisit: EventReplier.HomeTabVisitedReplier? = null
    private var onBotJoinChannel: EventReplier.BotJoinedChannelReplier? = null

    override fun register(channelProtocol: ChannelProtocol) {
        channelProtocols[channelProtocol.channel] = channelProtocol
    }

    override fun register(commandProtocol: CommandProtocol) {
        commandProtocols[commandProtocol.command] = commandProtocol
    }

    override fun register(privateMessageReplier: EventReplier.PrivateMessageReceivedReplier) {
        onPrivateMessage = privateMessageReplier
    }

    override fun register(homeTabVisitReplier: EventReplier.HomeTabVisitedReplier) {
        onHomeTabVisit = homeTabVisitReplier
    }

    override fun register(botJoinedChannelReplier: EventReplier.BotJoinedChannelReplier) {
        onBotJoinChannel = botJoinedChannelReplier
    }

    fun buildBot() = Bot(
        channelProtocols = channelProtocols,
        commandProtocols = commandProtocols,
        onPrivateMessage = onPrivateMessage,
        onHomeTabVisit = onHomeTabVisit,
        onBotJoinChannel = onBotJoinChannel
    )
}