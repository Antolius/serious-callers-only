package org.github.jantolis.seriouscallersonly.bot

import com.slack.api.methods.request.chat.ChatPostEphemeralRequest
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.chat.ChatUpdateRequest
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

fun Conversation.mapToPublicMessage(message: Reply.Message): ChatPostMessageRequest {
    clearInteractions()
    return ChatPostMessageRequest.builder()
            .channel(channel.id)
            .threadTs(thread?.id)
            .blocks(message.blocks.mapNotNull { it.toSlackBlock(this) })
            .build()
}

fun Conversation.mapToEphemeralMessage(message: Reply.Message, user: User): ChatPostEphemeralRequest {
    clearInteractions()
    return ChatPostEphemeralRequest.builder()
            .channel(channel.id)
            .threadTs(thread?.id)
            .user(user.id)
            .blocks(message.blocks.mapNotNull { it.toSlackBlock(this) })
            .build()
}

fun Conversation.mapToUpdateMessage(message: Reply.ReplacementMessage, tsToUpdate: String): ChatUpdateRequest {
    clearInteractions()
    return ChatUpdateRequest.builder()
            .channel(channel.id)
            .ts(tsToUpdate)
            .blocks(message.blocks.mapNotNull { it.toSlackBlock(this) })
            .build()
}

private fun MessageBlock.toSlackBlock(ctx: Conversation) =
        when (this) {
            is Block.Actions -> this.toSlackBlock(ctx)
            is Block.Context -> this.toSlackBlock(ctx)
            is Block.Divider -> this.toSlackBlock(ctx)
            is Block.Image -> this.toSlackBlock(ctx)
            is Block.Section -> this.toSlackBlock(ctx)
            else -> null
        }

private fun Block.Section.toSlackBlock(ctx: Conversation): SectionBlock =
        SectionBlock.builder()
                .text(text.toSlackElement(ctx))
                .fields(fields?.map { it.toSlackElement(ctx) })
                .accessory(accessory?.toSlackElement(ctx))
                .build()

private fun Block.Image.toSlackBlock(ctx: Conversation) =
        ImageBlock.builder()
                .imageUrl(url.toExternalForm())
                .altText(altText)
                .title(title?.toSlackElement(ctx))
                .build()

private fun Block.Divider.toSlackBlock(ctx: Conversation) = DividerBlock()

private fun Block.Context.toSlackBlock(ctx: Conversation) =
        ContextBlock.builder()
                .elements(elements.mapNotNull { it.toSlackElement(ctx) })
                .build()

private fun Block.Actions.toSlackBlock(ctx: Conversation): ActionsBlock =
        ActionsBlock.builder()
                .elements(elements.mapNotNull { it.toSlackElement(ctx) })
                .build()

private fun ContextElement.toSlackElement(ctx: Conversation) =
        when (this) {
            is Element.Text.Plain -> this.toSlackElement(ctx)
            is Element.Text.Markdown -> this.toSlackElement(ctx)
            is Element.Image -> this.toSlackElement(ctx)
            else -> null
        }

private fun SectionElement.toSlackElement(ctx: Conversation) =
        when (this) {
            is Element.Button -> this.toSlackElement(ctx)
            is Element.Image -> this.toSlackElement(ctx)
            is Element.Select -> this.toSlackElement(ctx)
            is Element.Overflow -> this.toSlackElement(ctx)
            else -> null
        }

private fun ActionElement.toSlackElement(ctx: Conversation) =
        when (this) {
            is Element.Button -> this.toSlackElement(ctx)
            is Element.Overflow -> this.toSlackElement(ctx)
            else -> null
        }

private fun Element.Text.toSlackElement(ctx: Conversation) =
        when (this) {
            is Element.Text.Plain -> this.toSlackElement(ctx)
            is Element.Text.Markdown -> this.toSlackElement(ctx)
        }

private fun Element.Text.Plain.toSlackElement(ctx: Conversation) =
        PlainTextObject.builder()
                .text(text)
                .emoji(emoji)
                .build()

private fun Element.Text.Markdown.toSlackElement(ctx: Conversation) =
        MarkdownTextObject.builder()
                .text(text)
                .verbatim(verbatim)
                .build()

private fun Element.Image.toSlackElement(ctx: Conversation) =
        ImageElement.builder()
                .imageUrl(url.toExternalForm())
                .altText(altText)
                .build()

private fun Element.Button.toSlackElement(ctx: Conversation): ButtonElement {
    val elementId = UUID.randomUUID().toString()
    ctx.register(LiveInteraction(
            key = InteractionKey(elementId = elementId, value = text.text),
            replier = onClick,
            validator = validate
    ))
    return ButtonElement.builder()
            .actionId(elementId)
            .text(text.toSlackElement(ctx))
            .style(style.style)
            .build()
}

private fun Element.Select.toSlackElement(ctx: Conversation): StaticSelectElement {
    val elementId = UUID.randomUUID().toString()
    return StaticSelectElement.builder()
            .actionId(elementId)
            .placeholder(placeholder.toSlackElement(ctx))
            .options(options.map { it.toSlackOption(ctx, elementId) })
            .build()
}

private fun Element.Overflow.toSlackElement(ctx: Conversation): OverflowMenuElement {
    val elementId = UUID.randomUUID().toString()
    return OverflowMenuElement.builder()
            .actionId(elementId)
            .options(options.map { it.toSlackOption(ctx, elementId) })
            .build()
}

private fun Option.toSlackOption(
        ctx: Conversation,
        elementId: String
): OptionObject {
    val value = UUID.randomUUID().toString()
    ctx.register(LiveInteraction(
            key = InteractionKey(elementId = elementId, value = value),
            replier = onSelect
    ))
    return OptionObject.builder()
            .value(value)
            .text(text.toSlackElement(ctx))
            .description(description?.toSlackElement(ctx))
            .build()
}