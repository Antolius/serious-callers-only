package hr.from.josipantolis.seriouscallersonly.runtime.slack

import com.slack.api.methods.AsyncMethodsClient
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.chat.ChatUpdateRequest
import com.slack.api.methods.request.views.ViewsOpenRequest
import com.slack.api.methods.response.chat.ChatPostEphemeralResponse
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.methods.response.chat.ChatUpdateResponse
import com.slack.api.methods.response.views.ViewsOpenResponse
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.PlainTextObject
import hr.from.josipantolis.seriouscallersonly.api.Errors
import hr.from.josipantolis.seriouscallersonly.api.Reply
import hr.from.josipantolis.seriouscallersonly.api.User
import hr.from.josipantolis.seriouscallersonly.api.Visibility
import hr.from.josipantolis.seriouscallersonly.runtime.repo.Repo
import kotlinx.coroutines.future.await

class AsyncSlackClient(private val delegate: AsyncMethodsClient) : SlackClient {
    override suspend fun chatPostEphemeral(req: ChatPostEphemeralRequest): ChatPostEphemeralResponse =
        delegate.chatPostEphemeral(req).await()

    override suspend fun chatPostMessage(req: ChatPostMessageRequest): ChatPostMessageResponse =
        delegate.chatPostMessage(req).await()

    override suspend fun chatUpdate(req: ChatUpdateRequest): ChatUpdateResponse =
        delegate.chatUpdate(req).await()

    override suspend fun viewOpen(req: ViewsOpenRequest): ViewsOpenResponse =
        delegate.viewsOpen(req).await()
}

interface SlackClient {
    suspend fun chatPostEphemeral(req: ChatPostEphemeralRequest): ChatPostEphemeralResponse
    suspend fun chatPostMessage(req: ChatPostMessageRequest): ChatPostMessageResponse
    suspend fun chatUpdate(req: ChatUpdateRequest): ChatUpdateResponse
    suspend fun viewOpen(req: ViewsOpenRequest): ViewsOpenResponse
}

class MessageSender(
    private val conversations: Repo<ConversationKey, Conversation>,
    private val client: SlackClient
) {
    suspend fun sendReply(reply: Reply, conversation: Conversation) {
        when (reply) {
            is Reply.Message -> sendNewMessage(reply, conversation)
            is Reply.ReplacementMessage -> updatePreviouslySentMessage(reply, conversation)
            is Reply.Modal -> showModal(reply, conversation)
            is Reply.NoOp -> {}
        }
        cleanup(conversation)
    }

    suspend fun sendErrors(errors: Errors, conversation: Conversation) {
        val slackMsg = buildSlackErrorMsg(errors, conversation)
        val slackResp = client.chatPostMessage(slackMsg)
        if (!slackResp.isOk) {
            throw Exception(slackResp.error)
        }
        conversation.messageTsToDelete = slackResp.ts
    }

    private suspend fun sendNewMessage(reply: Reply.Message, conversation: Conversation) {
        when (val visibleTo = reply.visibleTo) {
            is Visibility.Ephemeral -> sendEphemeralMessage(reply, visibleTo.user, conversation)
            Visibility.Public -> sendPublicMessage(reply, conversation)
        }
        conversation.responseReplier = reply.onReply
        if (!conversation.isCompleted()) conversations.store(conversation)
        val andThen = reply.andThen
        if (andThen != null) sendReply(andThen.cb(), conversation)
    }

    private suspend fun cleanup(conversation: Conversation) {
        if (conversation.isCompleted()) {
            conversation.key?.also { conversations.remove(it) }
        }
    }

    private suspend fun sendEphemeralMessage(msg: Reply.Message, user: User, conversation: Conversation) {
        val slackMsg = conversation.mapToEphemeralMessage(msg, user)
        val slackResp = client.chatPostEphemeral(slackMsg)
        if (!slackResp.isOk) {
            throw Exception(slackResp.error)
        }
        conversation.key = ConversationKey(conversation.channel, slackResp.messageTs)
    }

    private suspend fun sendPublicMessage(msg: Reply.Message, conversation: Conversation) {
        val slackMsg = conversation.mapToPublicMessage(msg)
        val slackResp = client.chatPostMessage(slackMsg)
        if (!slackResp.isOk) {
            throw Exception(slackResp.error)
        }
        conversation.updateableMessageTs = slackResp.ts
        conversation.key = ConversationKey(conversation.channel, slackResp.message.threadTs ?: slackResp.message.ts)
    }

    private suspend fun updatePreviouslySentMessage(
        replacementMsg: Reply.ReplacementMessage,
        conversation: Conversation
    ) {
        val tsOfMessageToUpdate = conversation.updateableMessageTs ?: return
        val slackMsg = conversation.mapToUpdateMessage(replacementMsg, tsOfMessageToUpdate)
        val slackResp = client.chatUpdate(slackMsg)
        if (!slackResp.isOk) {
            throw Exception(slackResp.error)
        }
        conversation.updateableMessageTs = slackResp.ts
        val key = ConversationKey(conversation.channel, slackResp.ts)
        conversation.key = key
        conversation.responseReplier = replacementMsg.onReply
        if (!conversation.isCompleted()) conversations.store(conversation)
        replacementMsg.andThen?.also { sendReply(it.cb(), conversation) }
    }

    private fun buildSlackErrorMsg(
        errors: Errors,
        conversation: Conversation
    ): ChatPostMessageRequest {
        val errorTxt = errors.errs.joinToString(
            separator = "\n:warning: ",
            prefix = ":warning: "
        )
        return ChatPostMessageRequest.builder()
            .channel(conversation.channel.id)
            .threadTs(conversation.thread?.id)
            .blocks(
                Blocks.asBlocks(
                    SectionBlock.builder()
                        .text(PlainTextObject.builder().text(errorTxt).build())
                        .build()
                )
            ).build()
    }

    private suspend fun showModal(modal: Reply.Modal, conversation: Conversation) {
        val triggerId = conversation.triggerId
            ?: throw Exception("Can't open modal without triggerId, modal: $modal, conv: $conversation")
        val slackModal = conversation.mapToModal(modal, triggerId)
        val slackResp = client.viewOpen(slackModal)
        if (!slackResp.isOk) {
            throw Exception(slackResp.error)
        }
        conversation.updateableModalId = slackResp.view.id
    }

}