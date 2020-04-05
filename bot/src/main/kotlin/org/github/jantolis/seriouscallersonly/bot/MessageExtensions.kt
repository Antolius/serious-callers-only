package org.github.jantolis.seriouscallersonly.bot

import com.slack.api.model.block.ActionsBlock
import com.slack.api.model.block.ImageBlock
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.model.block.composition.OptionObject
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.StaticSelectElement
import org.github.jantolis.seriouscallersonly.dsl.Content
import org.github.jantolis.seriouscallersonly.dsl.MsgElement
import java.util.*

fun Content.Message.toBlocks(replierRegisterer: (replier: ActionReplier) -> Unit): List<LayoutBlock> =
        elements.map { it.toSection(replierRegisterer) }

private fun MsgElement.toSection(replierRegisterer: (replier: ActionReplier) -> Unit) = when (this) {
    is MsgElement.Text -> SectionBlock.builder().text(toTextObject()).build()
    is MsgElement.Image -> ImageBlock.builder()
            .altText(altText)
            .imageUrl("$url")
            .title(title?.toPlainText())
            .build()
    is MsgElement.Selection -> ActionsBlock.builder()
            .blockId("selection")
            .elements(listOf(toBlockElement(replierRegisterer)))
            .build()
}

private fun MsgElement.Text.toTextObject() = if (this.markdown) {
    MarkdownTextObject(this.text, true)
} else {
    this.text.toPlainText()
}

private fun String.toPlainText() = PlainTextObject(this, true)

private fun MsgElement.Selection.toBlockElement(replierRegisterer: (replier: ActionReplier) -> Unit): StaticSelectElement {
    val replier = ActionReplier(UUID.randomUUID().toString())
    val staticSelect = StaticSelectElement.builder()
            .actionId(replier.actionId)
            .placeholder(placeholder.toPlainText())
            .options(options.map {
                val value = UUID.randomUUID().toString()
                replier.replies[value] = it.reply
                OptionObject(
                        it.text.toPlainText(),
                        value,
                        it.description?.toPlainText(),
                        null
                )
            })
            .build()
    replierRegisterer(replier)
    return staticSelect
}

