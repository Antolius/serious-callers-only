package hr.from.josipantolis.seriouscallersonly.api.script

import hr.from.josipantolis.seriouscallersonly.api.ChannelProtocol
import hr.from.josipantolis.seriouscallersonly.api.CommandProtocol
import java.util.*
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

@KotlinScript(
    displayName = "SCO Script",
    fileExtension = "sco.kts",
    compilationConfiguration = ScoCompilationConfig::class
)
abstract class ScoScript(val ctx: ScoScriptCtx) : ScoScriptCtx by ctx

interface ScoScriptCtx {
    val env: Properties
    fun register(channelProtocol: ChannelProtocol)
    fun register(commandProtocol: CommandProtocol)
}

internal object ScoCompilationConfig : ScriptCompilationConfiguration({
    jvm {
        dependenciesFromClassContext(
            ScoScript::class,
            "api",
            "kotlin-stdlib"
        )
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }

    defaultImports(
        hr.from.josipantolis.seriouscallersonly.api.Reply::class,
        hr.from.josipantolis.seriouscallersonly.api.Replier::class,
        hr.from.josipantolis.seriouscallersonly.api.Visibility::class,
        ChannelProtocol::class,
        CommandProtocol::class,
        CommandProtocol::class,
        hr.from.josipantolis.seriouscallersonly.api.Block::class,
        hr.from.josipantolis.seriouscallersonly.api.Element::class,
        hr.from.josipantolis.seriouscallersonly.api.ButtonStyle::class,
        hr.from.josipantolis.seriouscallersonly.api.Option::class,
        hr.from.josipantolis.seriouscallersonly.api.Command::class,
        hr.from.josipantolis.seriouscallersonly.api.Interaction::class,
        hr.from.josipantolis.seriouscallersonly.api.Validator::class
    )
})
