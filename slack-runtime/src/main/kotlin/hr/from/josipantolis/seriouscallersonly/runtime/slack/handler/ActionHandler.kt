package hr.from.josipantolis.seriouscallersonly.runtime.slack.handler

import com.slack.api.bolt.context.builtin.ActionContext
import com.slack.api.bolt.handler.builtin.BlockActionHandler
import com.slack.api.bolt.request.builtin.BlockActionRequest
import com.slack.api.bolt.response.Response
import hr.from.josipantolis.seriouscallersonly.api.Event
import hr.from.josipantolis.seriouscallersonly.runtime.repo.Repo
import hr.from.josipantolis.seriouscallersonly.runtime.slack.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import java.time.Clock

class ActionHandler(
    private val clock: Clock,
    private val conversations: Repo<ConversationKey, Conversation>
) : BlockActionHandler {
    override fun apply(req: BlockActionRequest, ctx: ActionContext): Response {
        GlobalScope.launch {
            handle(req, ctx)
        }
        return ctx.ack()
    }

    private suspend fun handle(req: BlockActionRequest, ctx: ActionContext) {
        val event = req.mapToEvent(clock) ?: return
        val convKey = req.mapToConversationKey()
        val conversation = conversations.find(convKey) ?: return
        val interactionKey = req.mapToInteractionKey() ?: return
        if (conversation.messageTsToDelete != null) {
            delete(ctx, conversation)
        }
        conversation.triggerId = req.payload.triggerId
        try {
            if (validate(conversation, event, interactionKey, ctx)) return
            reply(conversation, event, interactionKey, ctx)
        } finally {
            conversation.triggerId = null
            conversation.clearLiveInteractionsFor(interactionKey.actionId)
            val key = conversation.key
            if (conversation.isCompleted() && key != null) {
                conversations.remove(key)
            }
        }
    }

    private suspend fun delete(ctx: ActionContext, conversation: Conversation) {
        ctx.asyncClient().chatDelete {
            it.channel(conversation.channel.id)
            it.ts(conversation.messageTsToDelete)
        }.await()
        conversation.messageTsToDelete = null
    }

    private suspend fun validate(
        conversation: Conversation,
        event: Event.Interaction,
        interactionKey: InteractionKey,
        ctx: ActionContext
    ): Boolean {
        val errors = conversation.validate(event, interactionKey)
        if (errors != null && errors.isNotEmpty()) {
            MessageSender(
                conversations,
                AsyncSlackClient(ctx.asyncClient())
            )
                .sendErrors(errors, conversation)
            return true
        }
        return false
    }

    private suspend fun reply(
        conversation: Conversation,
        event: Event.Interaction,
        interactionKey: InteractionKey,
        ctx: ActionContext
    ) {
        val reply = conversation.renderReply(event, interactionKey)
        if (reply != null) {
            MessageSender(
                conversations,
                AsyncSlackClient(ctx.asyncClient())
            )
                .sendReply(reply, conversation)
        }
    }
}