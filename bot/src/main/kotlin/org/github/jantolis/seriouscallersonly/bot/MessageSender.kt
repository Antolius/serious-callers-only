package org.github.jantolis.seriouscallersonly.bot

import com.slack.api.methods.AsyncMethodsClient
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest
import com.slack.api.methods.request.chat.ChatPostMessageRequest
import com.slack.api.methods.request.chat.ChatUpdateRequest
import com.slack.api.methods.response.chat.ChatPostEphemeralResponse
import com.slack.api.methods.response.chat.ChatPostMessageResponse
import com.slack.api.methods.response.chat.ChatUpdateResponse
import com.slack.api.model.block.Blocks
import com.slack.api.model.block.SectionBlock
import com.slack.api.model.block.composition.PlainTextObject
import kotlinx.coroutines.future.await
import org.github.jantolis.seriouscallersonly.bot.repository.Repo
import org.github.jantolis.seriouscallersonly.dsl.ChainableReply
import org.github.jantolis.seriouscallersonly.dsl.Errors
import org.github.jantolis.seriouscallersonly.dsl.Reply
import org.github.jantolis.seriouscallersonly.dsl.Visibility

class AsyncSlackClient(private val delegate: AsyncMethodsClient) : SlackClient {
    override suspend fun chatPostEphemeral(req: ChatPostEphemeralRequest): ChatPostEphemeralResponse =
            delegate.chatPostEphemeral(req).await()

    override suspend fun chatPostMessage(req: ChatPostMessageRequest): ChatPostMessageResponse =
            delegate.chatPostMessage(req).await()

    override suspend fun chatUpdate(req: ChatUpdateRequest): ChatUpdateResponse =
            delegate.chatUpdate(req).await()
}

interface SlackClient {
    suspend fun chatPostEphemeral(req: ChatPostEphemeralRequest): ChatPostEphemeralResponse
    suspend fun chatPostMessage(req: ChatPostMessageRequest): ChatPostMessageResponse
    suspend fun chatUpdate(req: ChatUpdateRequest): ChatUpdateResponse
}

class MessageSender(
        private val convRepo: Repo<ConversationKey, Conversation>,
        private val client: SlackClient
) {
    suspend fun sendReply(reply: Reply, conv: Conversation) {
        clearPreExistingConversations(conv)
        when (reply) {
            is Reply.Message -> {
                when (val visibleTo = reply.visibleTo) {
                    is Visibility.Ephemeral -> sendEphemeralMessage(reply, visibleTo, conv)
                    Visibility.Public -> sendPublicMessage(reply, conv)
                }
                sendChainedReply(reply, conv)
            }
            is Reply.ReplacementMessage -> updatePreviouslySentMessage(reply, conv)
        }
    }

    suspend fun sendErrors(errors: Errors, conv: Conversation) {
        val errorTxt = errors.joinToString(
                separator = "\n:warning: ",
                prefix = ":warning: "
        )
        val slackMsg = ChatPostMessageRequest.builder()
                .channel(conv.channel.id)
                .threadTs(conv.thread?.id)
                .blocks(Blocks.asBlocks(SectionBlock.builder()
                        .text(PlainTextObject.builder().text(errorTxt).build())
                        .build()
                )).build()
        val slackResp = client.chatPostMessage(slackMsg)
        if (!slackResp.isOk) {
            throw Exception(slackResp.error)
        }
        conv.messageTsToDelete = slackResp.ts
    }

    private suspend fun clearPreExistingConversations(conv: Conversation) {
        conv.key.also { convRepo.remove(it) }
    }

    private suspend fun sendEphemeralMessage(msg: Reply.Message, visibleTo: Visibility.Ephemeral, conv: Conversation) {
        val slackMsg = conv.mapToEphemeralMessage(msg, visibleTo.user)
        val slackResp = client.chatPostEphemeral(slackMsg)
        if (!slackResp.isOk) {
            throw Exception(slackResp.error)
        }
        val key = ConversationKey(conv.channel, slackResp.messageTs)
        conv.key = key
        convRepo.store(conv)
    }

    private suspend fun sendPublicMessage(msg: Reply.Message, conv: Conversation) {
        val slackMsg = conv.mapToPublicMessage(msg)
        val slackResp = client.chatPostMessage(slackMsg)
        if (!slackResp.isOk) {
            throw Exception(slackResp.error)
        }
        conv.updateableMessageTs = slackResp.ts
        val key = ConversationKey(conv.channel, slackResp.message.ts)
        conv.key = key
        convRepo.store(conv)
    }

    private suspend fun updatePreviouslySentMessage(replacementMsg: Reply.ReplacementMessage, conv: Conversation) {
        val tsOfMessageToUpdate = conv.updateableMessageTs ?: return
        val slackMsg = conv.mapToUpdateMessage(replacementMsg, tsOfMessageToUpdate)
        val slackResp = client.chatUpdate(slackMsg)
        if (!slackResp.isOk) {
            throw Exception(slackResp.error)
        }
        conv.updateableMessageTs = slackResp.ts
        val key = ConversationKey(conv.channel, slackResp.ts)
        conv.key = key
        convRepo.store(conv)
        sendChainedReply(replacementMsg, conv)
    }

    private suspend fun sendChainedReply(chainableReply: ChainableReply, conv: Conversation) {
        chainableReply.andThen?.also { sendReply(it.replier(null), conv) }
    }
}