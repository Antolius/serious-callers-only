package hr.from.josipantolis.seriouscallersonly.api.dsl

import hr.from.josipantolis.seriouscallersonly.api.*
import java.net.URL

@DslMarker
annotation class BotMarker

@BotMarker
interface CallScriptExtensions {
    val String.channel: Channel
        get() = Channel(id = this)

    val String.user: User
        get() = User(id = this)

    val String.cmd: Command
        get() = Command(cmd = this)
    
    fun channelProtocol(channel: Channel, init: ChannelProtocolBuilder.() -> Unit): ChannelProtocol
    fun commandProtocol(
        cmd: Command,
        cb: suspend ReplierCtx.(msg: Event.CommandInvoked) -> ReplyBuilder
    ): CommandProtocol

    fun privateMessageReplier(
        cb: suspend ReplierCtx.(msg: Event.PrivateMessageReceived) -> ReplyBuilder
    ): EventReplier.PrivateMessageReceivedReplier

    fun homeTabVisitReplier(
        cb: suspend ReplierCtx.(msg: Event.HomeTabVisited) -> ReplyBuilder
    ): EventReplier.HomeTabVisitedReplier

    fun botJoinedChannelReplier(
        cb: suspend ReplierCtx.(msg: Event.BotJoinedChannel) -> ReplyBuilder
    ): EventReplier.BotJoinedChannelReplier
}

class CallScriptExtensionsRoot : CallScriptExtensions {
    override fun channelProtocol(channel: Channel, init: ChannelProtocolBuilder.() -> Unit): ChannelProtocol {
        val builder = ChannelProtocolBuilder(channel)
        builder.init()
        return builder.build()
    }

    override fun commandProtocol(
        cmd: Command,
        cb: suspend ReplierCtx.(msg: Event.CommandInvoked) -> ReplyBuilder
    ) = CommandProtocol(
        command = cmd,
        onCommandInvoked = EventReplier.CommandInvokedReplier { ReplierCtx().cb(it).build() }
    )

    override fun privateMessageReplier(
        cb: suspend ReplierCtx.(msg: Event.PrivateMessageReceived) -> ReplyBuilder
    ) = EventReplier.PrivateMessageReceivedReplier { ReplierCtx().cb(it).build() }

    override fun homeTabVisitReplier(
        cb: suspend ReplierCtx.(msg: Event.HomeTabVisited) -> ReplyBuilder
    ) = EventReplier.HomeTabVisitedReplier { ReplierCtx().cb(it).build() }

    override fun botJoinedChannelReplier(
        cb: suspend ReplierCtx.(msg: Event.BotJoinedChannel) -> ReplyBuilder
    ) = EventReplier.BotJoinedChannelReplier { ReplierCtx().cb(it).build() }
}

@BotMarker
class ChannelProtocolBuilder(private val channel: Channel) {
    private var timerProtocol: TimerProtocol? = null
    private var onPublicMessage: EventReplier.PublicMessagePostedReplier? = null
    private var onUserJoined: EventReplier.UserJoinedChannelReplier? = null

    fun build() = ChannelProtocol(
        channel = channel,
        onUserJoined = onUserJoined,
        onPublicMessage = onPublicMessage,
        timerProtocol = timerProtocol
    )

    fun onTimer(cron: String, cb: suspend ReplierCtx.(msg: Event.Timer) -> ReplyBuilder) {
        timerProtocol = TimerProtocol(
            cron = cron,
            onTimer = EventReplier.TimerReplier { ReplierCtx().cb(it).build() }
        )
    }

    fun onPublicMessage(cb: suspend ReplierCtx.(msg: Event.PublicMessagePosted) -> ReplyBuilder) {
        onPublicMessage = EventReplier.PublicMessagePostedReplier { ReplierCtx().cb(it).build() }
    }

    fun onUserJoined(cb: suspend ReplierCtx.(msg: Event.UserJoinedChannel) -> ReplyBuilder) {
        onUserJoined = EventReplier.UserJoinedChannelReplier { ReplierCtx().cb(it).build() }
    }
}

@BotMarker
interface ReplyBuilder {
    suspend fun build(): Reply
}

@BotMarker
open class ReplierCtx {

    suspend fun replyPublicly(init: suspend MessageBlocksBuilder.() -> Unit): MessageBuilder {
        val blocks = MessageBlocksBuilder()
        blocks.init()
        return MessageBuilder(Visibility.Public, blocks)
    }

    suspend fun replyPrivatelyTo(user: User, init: suspend MessageBlocksBuilder.() -> Unit): MessageBuilder {
        val blocks = MessageBlocksBuilder()
        blocks.init()
        return MessageBuilder(Visibility.Ephemeral(user), blocks)
    }

    suspend fun replacePreviousMessageWith(init: suspend MessageBlocksBuilder.() -> Unit): ReplacementMessageBuilder {
        val blocks = MessageBlocksBuilder()
        blocks.init()
        return ReplacementMessageBuilder(blocks)
    }

}

@BotMarker
class MessageBlocksBuilder {
    private val blocks = mutableListOf<MessageBlockBuilder>()

    suspend fun build() = blocks.map { it.build() }

    val String.md: Element.Text.Markdown
        get() = Element.Text.Markdown(text = this)

    val String.txt: Element.Text.Plain
        get() = Element.Text.Plain(text = this)

    val String.url: URL
        get() = URL(this)

    operator fun MessageBlockBuilder.unaryPlus() {
        this@MessageBlocksBuilder.blocks += this
    }

    val divider = object : MessageBlockBuilder {
        override suspend fun build() = Block.Divider
    }

    suspend fun section(
        txt: Element.Text.Plain,
        init: suspend SectionBlockBuilder.() -> Unit = {}
    ): SectionBlockBuilder {
        val builder = SectionBlockBuilder(txt)
        builder.init()
        return builder
    }

    suspend fun section(
        md: Element.Text.Markdown,
        init: suspend SectionBlockBuilder.() -> Unit = {}
    ): SectionBlockBuilder {
        val builder = SectionBlockBuilder(md)
        builder.init()
        return builder
    }

    suspend fun actions(init: suspend ActionsBlockBuilder.() -> Unit): ActionsBlockBuilder {
        val builder = ActionsBlockBuilder()
        builder.init()
        return builder
    }

    suspend fun context(init: suspend ContextBlockBuilder.() -> Unit): ContextBlockBuilder {
        val builder = ContextBlockBuilder()
        builder.init()
        return builder
    }

    fun image(img: Pair<URL, String>) = object : MessageBlockBuilder {
        override suspend fun build(): Block.Image {
            val (url, altText) = img
            return Block.Image(url = url, altText = altText)
        }
    }
}

interface AndThener : ReplyBuilder {
    suspend fun andThen(cb: suspend ReplierCtx.() -> ReplyBuilder): OnResponser
}

interface OnResponser : ReplyBuilder {
    suspend fun onResponse(cb: suspend ReplierCtx.(msg: Event.Interaction.UserResponded) -> ReplyBuilder): AndThener
}

abstract class ChainableReplyBuilder : AndThener, OnResponser {
    protected var andThen: Replier? = null
    protected var onReply: EventReplier.InteractionReplier.UserRespondedReplier? = null

    override suspend fun andThen(cb: suspend ReplierCtx.() -> ReplyBuilder): OnResponser {
        andThen = Replier { ReplierCtx().cb().build() }
        return this
    }

    override suspend fun onResponse(cb: suspend ReplierCtx.(msg: Event.Interaction.UserResponded) -> ReplyBuilder): AndThener {
        onReply = EventReplier.InteractionReplier.UserRespondedReplier { ReplierCtx().cb(it).build() }
        return this
    }
}

class MessageBuilder(
    private val visibleTo: Visibility,
    private val blocks: MessageBlocksBuilder
) : ChainableReplyBuilder() {

    override suspend fun build() = Reply.Message(
        blocks = blocks.build(),
        visibleTo = visibleTo,
        andThen = andThen,
        onReply = onReply
    )
}

class ReplacementMessageBuilder(private val blocks: MessageBlocksBuilder) : ChainableReplyBuilder() {
    override suspend fun build() = Reply.ReplacementMessage(
        blocks = blocks.build(),
        andThen = andThen,
        onReply = onReply
    )
}

@BotMarker
interface MessageBlockBuilder {
    suspend fun build(): MessageBlock
}

class SectionBlockBuilder(private val text: Element.Text) : MessageBlockBuilder {
    private val fields = mutableListOf<Element.Text>()
    var accessory: AccessoryBuilder? = null

    val String.md: Element.Text.Markdown
        get() = Element.Text.Markdown(text = this)

    val String.txt: Element.Text.Plain
        get() = Element.Text.Plain(text = this)

    val String.url: URL
        get() = URL(this)

    operator fun Element.Text.unaryPlus() {
        fields += this
    }

    override suspend fun build() = Block.Section(
        text = text,
        fields = if (fields.isNotEmpty()) fields else null,
        accessory = accessory?.build()
    )

    fun image(img: Pair<URL, String>) = object : AccessoryBuilder {
        override suspend fun build(): SectionElement {
            val (url, altText) = img
            return Element.Image(url, altText)
        }
    }

    val button = ButtonPicker()

    suspend fun select(txt: Element.Text.Plain, init: suspend SelectBuilder.() -> Unit): AccessoryBuilder {
        val builder = SelectBuilder(txt)
        builder.init()
        return builder
    }

    suspend fun overflow(init: suspend OverflowBuilder.() -> Unit): AccessoryBuilder {
        val builder = OverflowBuilder()
        builder.init()
        return builder
    }
}

class ActionsBlockBuilder : MessageBlockBuilder {
    private val elements = mutableListOf<ActionBuilder>()

    operator fun ActionBuilder.unaryPlus() {
        this@ActionsBlockBuilder.elements += this
    }

    val String.txt: Element.Text.Plain
        get() = Element.Text.Plain(text = this)

    override suspend fun build() = Block.Actions(
        elements = elements.map { it.build() }
    )

    val button = ButtonPicker()

    suspend fun overflow(init: suspend OverflowBuilder.() -> Unit): ActionBuilder {
        val builder = OverflowBuilder()
        builder.init()
        return builder
    }

}

class ContextBlockBuilder : MessageBlockBuilder {
    private val elements = mutableListOf<ContextElement>()

    operator fun ContextElement.unaryPlus() {
        elements += this
    }

    val String.md: Element.Text.Markdown
        get() = Element.Text.Markdown(text = this)

    val String.txt: Element.Text.Plain
        get() = Element.Text.Plain(text = this)

    val String.url: URL
        get() = URL(this)

    fun image(img: Pair<URL, String>): ContextElement {
        val (url, altText) = img
        return Element.Image(url, altText)
    }

    override suspend fun build() = Block.Context(elements)

}

class ButtonPicker {

    suspend operator fun invoke(
        txt: Element.Text.Plain,
        cb: suspend ReplierCtx.(msg: Event.Interaction.ButtonClicked) -> ReplyBuilder
    ) =
        builder(txt, ButtonStyle.DEFAULT, cb)

    suspend fun primary(
        txt: Element.Text.Plain,
        cb: suspend ReplierCtx.(msg: Event.Interaction.ButtonClicked) -> ReplyBuilder
    ) =
        builder(txt, ButtonStyle.PRIMARY, cb)

    suspend fun danger(
        txt: Element.Text.Plain,
        cb: suspend ReplierCtx.(msg: Event.Interaction.ButtonClicked) -> ReplyBuilder
    ) =
        builder(txt, ButtonStyle.DANGER, cb)

    private suspend fun builder(
        txt: Element.Text.Plain,
        style: ButtonStyle,
        cb: suspend ReplierCtx.(msg: Event.Interaction.ButtonClicked) -> ReplyBuilder
    ) = object : ButtonBuilder {
        override suspend fun build() = Element.Button(
            text = txt,
            style = style,
            onClick = EventReplier.InteractionReplier.ButtonClickedReplier { ReplierCtx().cb(it).build() }
        )
    }
}

@BotMarker
interface AccessoryBuilder {
    suspend fun build(): SectionElement
}

@BotMarker
interface ActionBuilder {
    suspend fun build(): ActionElement
}

interface ButtonBuilder : AccessoryBuilder, ActionBuilder {
    override suspend fun build(): Element.Button
}

class SelectBuilder(private val txt: Element.Text.Plain) : AccessoryBuilder {
    private val options = mutableListOf<OptionBuilder>()

    operator fun OptionBuilder.unaryPlus() {
        options += this
    }

    val String.txt: Element.Text.Plain
        get() = Element.Text.Plain(text = this)


    override suspend fun build() = Element.Select(
        placeholder = txt,
        options = options.map { it.build() }
    )

    fun option(txt: Element.Text.Plain, cb: suspend ReplierCtx.(msg: Event.Interaction.OptionPicked) -> ReplyBuilder) =
        OptionBuilder(txt, cb)

}

class OptionBuilder(
    private val txt: Element.Text.Plain,
    private val cb: suspend ReplierCtx.(msg: Event.Interaction.OptionPicked) -> ReplyBuilder
) {
    suspend fun build() = Option(
        text = txt,
        onPick = EventReplier.InteractionReplier.OptionPickedReplier { ReplierCtx().cb(it).build() }
    )
}

class OverflowBuilder : AccessoryBuilder, ActionBuilder {
    private val options = mutableListOf<OptionBuilder>()

    operator fun OptionBuilder.unaryPlus() {
        options += this
    }

    val String.txt: Element.Text.Plain
        get() = Element.Text.Plain(text = this)

    fun option(txt: Element.Text.Plain, cb: suspend ReplierCtx.(msg: Event.Interaction.OptionPicked) -> ReplyBuilder) =
        OptionBuilder(txt, cb)

    override suspend fun build() = Element.Overflow(
        options = options.map { it.build() }
    )

}
