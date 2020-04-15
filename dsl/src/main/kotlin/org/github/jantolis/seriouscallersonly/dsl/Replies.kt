package org.github.jantolis.seriouscallersonly.dsl

sealed class Reply {
    class Message(
            val blocks: List<MessageBlock>,
            val visibleTo: Visibility = Visibility.Public,
            val andThen: VoidReplier? = null,
            val onReply: Replier<PostedMessage>? = null
    ) : Reply() {
        fun asReplacement() = ReplacementMessage(blocks, andThen, onReply)
    }

    class ReplacementMessage(
            val blocks: List<MessageBlock>,
            val andThen: VoidReplier? = null,
            val onReply: Replier<PostedMessage>? = null
    ) : Reply() {
        fun asNewMessage() = Message(blocks, andThen = andThen, onReply = onReply)
    }

    //    class Modal() : Reply()
//    class HomeTabs() : Reply()
}

class Replier<T>(val replier: suspend (t: T) -> Reply)
typealias VoidReplier = Replier<Void?>
class Validator(val validator: (interaction: Interaction) -> Errors)
typealias Errors = List<String>

class ChannelProtocol(
        val onUserJoinChannel: Replier<User>? = null,
        val onNewChannelMessage: Replier<PostedMessage>? = null
)

class CommandProtocol(val onSlashCommand: Replier<CommandInvocation>)

class Bot(
        val channelProtocols: Map<Channel, ChannelProtocol> = mapOf(),
        val commandProtocols: Map<Command, CommandProtocol> = mapOf(),
        val onNewPrivateMessage: Replier<PostedMessage>? = null,
        val onUserVisitHomeTab: Replier<User>? = null,
        val onBotJoinChannel: Replier<Channel>? = null
)

sealed class Visibility {
    class Ephemeral(val user: User) : Visibility()
    object Public : Visibility()
}