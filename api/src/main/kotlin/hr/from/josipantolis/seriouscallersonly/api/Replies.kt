package hr.from.josipantolis.seriouscallersonly.api

import hr.from.josipantolis.seriouscallersonly.api.Event.*
import hr.from.josipantolis.seriouscallersonly.api.Event.Interaction.*
import hr.from.josipantolis.seriouscallersonly.api.EventReplier.InteractionReplier.UserRespondedReplier

interface ChainableReply {
    val andThen: Replier?
    val onReply: UserRespondedReplier?
}

sealed class Reply {
    class Message(
        val blocks: List<MessageBlock>,
        val visibleTo: Visibility = Visibility.Public,
        override val andThen: Replier? = null,
        override val onReply: UserRespondedReplier? = null
    ) : Reply(), ChainableReply {
        fun asReplacement() = ReplacementMessage(blocks, andThen, onReply)
    }

    class ReplacementMessage(
        val blocks: List<MessageBlock>,
        override val andThen: Replier? = null,
        override val onReply: UserRespondedReplier? = null
    ) : Reply(), ChainableReply {
        fun asNewMessage() = Message(blocks, andThen = andThen, onReply = onReply)
    }

    class Modal(
        val title: Element.Text.Plain
    ) : Reply()

    class ModalUpdate(
        val title: Element.Text.Plain
    ) : Reply()
//    class HomeTabs() : Reply()
    object NoOp: Reply()
}

sealed class Visibility {
    class Ephemeral(val user: User) : Visibility()
    object Public : Visibility()
}

class Replier(val cb: suspend () -> Reply)

private typealias CB<E> = suspend (e: E) -> Reply

sealed class EventReplier<in E : Event>(
    open val cb: CB<E>
) {
    class TimerReplier(cb: CB<Timer>) : EventReplier<Timer>(cb)
    class UserJoinedChannelReplier(cb: CB<UserJoinedChannel>) : EventReplier<UserJoinedChannel>(cb)
    class PublicMessagePostedReplier(cb: CB<PublicMessagePosted>) : EventReplier<PublicMessagePosted>(cb)
    class CommandInvokedReplier(cb: CB<CommandInvoked>) : EventReplier<CommandInvoked>(cb)
    class BotJoinedChannelReplier(cb: CB<BotJoinedChannel>) : EventReplier<BotJoinedChannel>(cb)
    class PrivateMessageReceivedReplier(cb: CB<PrivateMessageReceived>) : EventReplier<PrivateMessageReceived>(cb)
    class HomeTabVisitedReplier(cb: CB<HomeTabVisited>) : EventReplier<HomeTabVisited>(cb)
    sealed class InteractionReplier<in I : Interaction>(cb: CB<I>) : EventReplier<I>(cb) {
        class ButtonClickedReplier(cb: CB<ButtonClicked>) : InteractionReplier<ButtonClicked>(cb)
        class OptionPickedReplier(cb: CB<OptionPicked>) : InteractionReplier<OptionPicked>(cb)
        class TextInputReplier(cb: CB<TextInput>) : InteractionReplier<TextInput>(cb)
        class UserRespondedReplier(cb: CB<UserResponded>) : InteractionReplier<UserResponded>(cb)
    }

    fun curry(e: E) = Replier { cb(e) }

    override fun toString(): String = this::class.simpleName ?: "UnknownReplier"
}

class Validator<in I : Interaction>(val check: (i: I) -> Errors) {
    operator fun invoke(i: I) = check(i)
}

data class Errors(val errs: List<String>) {
    fun isNotEmpty() = errs.isNotEmpty()
}