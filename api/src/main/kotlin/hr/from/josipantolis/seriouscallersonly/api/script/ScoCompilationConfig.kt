package hr.from.josipantolis.seriouscallersonly.api.script

import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

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
        hr.from.josipantolis.seriouscallersonly.api.Bot::class,
        hr.from.josipantolis.seriouscallersonly.api.ChannelProtocol::class,
        hr.from.josipantolis.seriouscallersonly.api.CommandProtocol::class,
        hr.from.josipantolis.seriouscallersonly.api.TimerProtocol::class,
        hr.from.josipantolis.seriouscallersonly.api.Event::class,
        hr.from.josipantolis.seriouscallersonly.api.ChainableReply::class,
        hr.from.josipantolis.seriouscallersonly.api.Reply::class,
        hr.from.josipantolis.seriouscallersonly.api.Visibility::class,
        hr.from.josipantolis.seriouscallersonly.api.Replier::class,
        hr.from.josipantolis.seriouscallersonly.api.EventReplier::class,
        hr.from.josipantolis.seriouscallersonly.api.Validator::class,
        hr.from.josipantolis.seriouscallersonly.api.Block::class,
        hr.from.josipantolis.seriouscallersonly.api.Element::class,
        hr.from.josipantolis.seriouscallersonly.api.ButtonStyle::class,
        hr.from.josipantolis.seriouscallersonly.api.Option::class,
        hr.from.josipantolis.seriouscallersonly.api.User::class,
        hr.from.josipantolis.seriouscallersonly.api.Channel::class,
        hr.from.josipantolis.seriouscallersonly.api.Thread::class,
        hr.from.josipantolis.seriouscallersonly.api.Command::class,
        hr.from.josipantolis.seriouscallersonly.api.MessageText::class,
        hr.from.josipantolis.seriouscallersonly.api.InputText::class
    )
})
