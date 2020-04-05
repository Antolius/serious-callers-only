package org.github.jantolis.seriouscallersonly.dsl

import java.net.URL
import kotlin.reflect.KClass

typealias Reply = ReplyCtx.() -> Unit
typealias ReplyToUserJoin = ReplyCtx.(usr: User) -> Unit
typealias ReplyToMessage = ReplyCtx.(msg: HistoryMessage) -> Unit

enum class Destination { PUBLIC, THREAD, IM, CONTINUE }

@ProtocolMarker
interface ReplyCtx : Conversational {
    var postTo: Destination

    fun <T : Content> reply(type: KClass<T>, init: T.() -> Unit)
}

@ProtocolMarker
sealed class Content {
    class Modal : Content()
    class Message : Content() {
        val elements: MutableList<MsgElement> = mutableListOf()

        fun text(text: String, markdown: Boolean = true) {
            elements += MsgElement.Text(text, markdown)
        }

        fun image(url: URL, altText: String, title: String? = null) {
            elements += MsgElement.Image(url, altText, title)
        }

        fun selection(placeholder: String, options: List<SelectionOption>) {
            elements += MsgElement.Selection(placeholder, options)
        }
    }
}

sealed class MsgElement {
    data class Text(val text: String, val markdown: Boolean = true) : MsgElement()
    data class Image(val url: URL, val altText: String, val title: String? = null) : MsgElement()
    data class Selection(val placeholder: String, val options: List<SelectionOption>) : MsgElement()
}

data class SelectionOption(
        val text: String,
        val description: String? = null,
        val reply: Reply
)