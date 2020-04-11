package org.github.jantolis.seriouscallersonly.bot

import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.model.block.*
import com.slack.api.model.block.composition.MarkdownTextObject
import com.slack.api.model.block.composition.OptionObject
import com.slack.api.model.block.composition.PlainTextObject
import com.slack.api.model.block.element.ButtonElement
import com.slack.api.model.block.element.ImageElement
import com.slack.api.model.block.element.OverflowMenuElement
import com.slack.api.model.block.element.StaticSelectElement
import org.github.jantolis.seriouscallersonly.dsl.*
import java.util.*

fun ConversationContext.mapToSlackMessage(message: Reply.Message): ChatPostMessageRequest =
        ChatPostMessageRequest.builder()
                .channel(conversation.channel.id)
                .threadTs(conversation.thread?.id)
                .blocks(message.blocks.mapNotNull { it.toSlackBlock(this) })
                .build()


private fun MessageBlock.toSlackBlock(ctx: ConversationContext) =
        when (this) {
            is Block.Actions -> this.toSlackBlock(ctx)
            is Block.Context -> this.toSlackBlock(ctx)
            is Block.Divider -> this.toSlackBlock(ctx)
            is Block.Image -> this.toSlackBlock(ctx)
            is Block.Section -> this.toSlackBlock(ctx)
            else -> null
        }

private fun Block.Section.toSlackBlock(ctx: ConversationContext): SectionBlock {
    val blockId = UUID.randomUUID().toString()
    return SectionBlock.builder()
            .text(text.toSlackElement(ctx))
            .fields(fields?.map { it.toSlackElement(ctx) })
            .accessory(accessory?.toSlackElement(ctx, blockId))
            .build()
}

private fun Block.Image.toSlackBlock(ctx: ConversationContext) =
        ImageBlock.builder()
                .imageUrl(url.toExternalForm())
                .altText(altText)
                .title(title?.toSlackElement(ctx))
                .build()

private fun Block.Divider.toSlackBlock(ctx: ConversationContext) = DividerBlock()

private fun Block.Context.toSlackBlock(ctx: ConversationContext) =
        ContextBlock.builder()
                .elements(elements.mapNotNull { it.toSlackElement(ctx) })
                .build()

private fun Block.Actions.toSlackBlock(ctx: ConversationContext): ActionsBlock {
    val blockId = UUID.randomUUID().toString()
    return ActionsBlock.builder()
            .elements(elements.mapNotNull { it.toSlackElement(ctx, blockId) })
            .build()
}

private fun ContextElement.toSlackElement(ctx: ConversationContext) =
        when (this) {
            is Element.Text.Plain -> this.toSlackElement(ctx)
            is Element.Text.Markdown -> this.toSlackElement(ctx)
            is Element.Image -> this.toSlackElement(ctx)
            else -> null
        }

private fun SectionElement.toSlackElement(ctx: ConversationContext, blockId: String) =
        when (this) {
            is Element.Button -> this.toSlackElement(ctx, blockId)
            is Element.Image -> this.toSlackElement(ctx)
            is Element.Select -> this.toSlackElement(ctx, blockId)
            is Element.Overflow -> this.toSlackElement(ctx, blockId)
            else -> null
        }

private fun ActionElement.toSlackElement(ctx: ConversationContext, blockId: String) =
        when (this) {
            is Element.Button -> this.toSlackElement(ctx, blockId)
            is Element.Overflow -> this.toSlackElement(ctx, blockId)
            else -> null
        }

private fun Element.Text.toSlackElement(ctx: ConversationContext) =
        when (this) {
            is Element.Text.Plain -> this.toSlackElement(ctx)
            is Element.Text.Markdown -> this.toSlackElement(ctx)
        }

private fun Element.Text.Plain.toSlackElement(ctx: ConversationContext) =
        PlainTextObject.builder()
                .text(text)
                .emoji(emoji)
                .build()

private fun Element.Text.Markdown.toSlackElement(ctx: ConversationContext) =
        MarkdownTextObject.builder()
                .text(text)
                .verbatim(verbatim)
                .build()

private fun Element.Image.toSlackElement(ctx: ConversationContext) =
        ImageElement.builder()
                .imageUrl(url.toExternalForm())
                .altText(altText)
                .build()

private fun Element.Button.toSlackElement(ctx: ConversationContext, blockId: String): ButtonElement {
    val elementId = UUID.randomUUID().toString()
    ctx.register(LiveInteraction(
            key = InteractionKey(blockId = blockId, elementId = elementId, value = text.text),
            replier = onClick,
            replacer = onClickReplaceWith
    ))
    return ButtonElement.builder()
            .actionId(elementId)
            .text(text.toSlackElement(ctx))
            .style(style.style)
            .build()
}

private fun Element.Select.toSlackElement(ctx: ConversationContext, blockId: String): StaticSelectElement {
    val elementId = UUID.randomUUID().toString()
    return StaticSelectElement.builder()
            .actionId(elementId)
            .placeholder(placeholder.toSlackElement(ctx))
            .options(options.map { it.toSlackOption(ctx, blockId, elementId, onSelectReplaceWith) })
            .build()
}

private fun Element.Overflow.toSlackElement(ctx: ConversationContext, blockId: String): OverflowMenuElement {
    val elementId = UUID.randomUUID().toString()
    return OverflowMenuElement.builder()
            .actionId(elementId)
            .options(options.map { it.toSlackOption(ctx, blockId, elementId, onSelectReplaceWith) })
            .build()
}

private fun Option.toSlackOption(
        ctx: ConversationContext,
        blockId: String,
        elementId: String,
        replacer: Replacer<Option>?
): OptionObject {
    val value = UUID.randomUUID().toString()
    ctx.register(LiveInteraction(
            key = InteractionKey(blockId = blockId, elementId = elementId, value = value),
            replier = onSelect,
            replacer = replacer
    ))
    return OptionObject.builder()
            .value(value)
            .text(text.toSlackElement(ctx))
            .description(description?.toSlackElement(ctx))
            .build()
}