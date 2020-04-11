package org.github.jantolis.seriouscallersonly.dsl

sealed class Reply {
    class Message(
            val blocks: List<MessageBlock>,
            val andThen: VoidReplier? = null,
            val onReply: Replier<PostedMessage>? = null
    ) : Reply()

    class ReplacementMessage(
            val blocks: List<MessageBlock>,
            val andThen: VoidReplier? = null,
            val onReply: Replier<PostedMessage>? = null
    ) : Reply()

    //    class Modal() : Reply()
//    class HomeTabs() : Reply()
}

class Replier<T>(val replier: suspend (t: T) -> Reply)
typealias VoidReplier = Replier<Void?>
class Replacer<T>(val replacer: suspend (t: T) -> Element)
class Validator<T>(val validator: (t: T) -> Errors)

typealias Errors = List<String>

class ChannelProtocol(
        val onUserJoinChannel: Replier<User>? = null,
        val onNewChannelMessage: Replier<PostedMessage>? = null
)

class CommandProtocol(val onSlashCommand: Replier<Command>)

class Bot(
        val channelProtocols: Map<Channel, ChannelProtocol> = mapOf(),
        val commandProtocols: Map<Command, ChannelProtocol> = mapOf(),
        val onNewPrivateMessage: Replier<PostedMessage>? = null,
//        val onUserVisitHomeTab: Replier<User>? = null,
        val onBotJoinChannel: Replier<Channel>? = null
)