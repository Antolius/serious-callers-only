package hr.from.josipantolis.seriouscallersonly.api

import java.net.URL

interface MessageBlock
interface ModalBlock
interface HomeTabBlock
interface UniversalBlock : MessageBlock, ModalBlock, HomeTabBlock

sealed class Block {
    class Actions(val elements: List<ActionElement>) : Block(), UniversalBlock
    class Context(val elements: List<ContextElement>) : Block(), UniversalBlock
    class Divider() : Block(), UniversalBlock
    class Image(
        val url: URL,
        val altText: String,
        val title: Element.Text.Plain? = null
    ) : Block(), UniversalBlock

    class Input(
        val label: Element.Text.Plain,
        val element: InputElement,
        val optional: Boolean = false,
        val hint: Element.Text.Plain? = null
    ) : Block(), ModalBlock

    class Section(
        val text: Element.Text,
        val fields: List<Element.Text>? = null,
        val accessory: SectionElement? = null
    ) : Block(), UniversalBlock
}



