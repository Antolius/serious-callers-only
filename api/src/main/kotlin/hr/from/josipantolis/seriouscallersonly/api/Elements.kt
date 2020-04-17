package hr.from.josipantolis.seriouscallersonly.api

import hr.from.josipantolis.seriouscallersonly.api.EventReplier.InteractionReplier.*
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
        val validate: Validator<Event.Interaction.ButtonClicked>? = null,
        val onClick: ButtonClickedReplier
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
        val validate: Validator<Event.Interaction.TextInput>? = null,
        val onInput: TextInputReplier
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
    val validate: Validator<Event.Interaction.OptionPicked>? = null,
    val onPick: OptionPickedReplier
)