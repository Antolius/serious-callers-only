package hr.from.josipantolis.seriouscallersonly.dsl

import java.net.URL

interface ContextElement
interface ActionElement
interface SectionElement
interface InputElement

sealed class Element {
    sealed class Text : Element(), ContextElement {
        class Plain(val text: String, val emoji: Boolean = true) : Text()
        class Markdown(val text: String, val verbatim: Boolean = false) : Text()
    }

    class Button(
            val text: Text.Plain,
            val style: ButtonStyle = ButtonStyle.DEFAULT,
            val validate: Validator? = null,
            val onClick: Replier<Interaction>
    ) : Element(), SectionElement, ActionElement

    class Image(
            val url: URL,
            val altText: String
    ) : Element(), SectionElement, ContextElement

    class Select(
            val placeholder: Text.Plain,
            val options: List<Option>
    ) : Element(), SectionElement, InputElement

    class Overflow(val options: List<Option>) : Element(), SectionElement, ActionElement

    class TextInput(
            val placeholder: Text.Plain? = null,
            val initialValue: String? = null,
            val multiline: Boolean = false,
            val validate: Validator
    ) : Element(), InputElement
}

enum class ButtonStyle(val style: String?) {
    DEFAULT(null),
    PRIMARY("primary"),
    DANGER("danger")
}

class Option(
        val text: Element.Text.Plain,
        val description: Element.Text.Plain? = null,
        val onSelect: Replier<Interaction>
)