package hr.from.josipantolis.seriouscallersonly.runtime.slack

import com.slack.api.bolt.request.builtin.BlockActionRequest
import hr.from.josipantolis.seriouscallersonly.api.Channel
import hr.from.josipantolis.seriouscallersonly.api.Event
import hr.from.josipantolis.seriouscallersonly.api.Event.Interaction.*
import hr.from.josipantolis.seriouscallersonly.api.InputText
import hr.from.josipantolis.seriouscallersonly.api.User
import java.time.Clock

fun BlockActionRequest.mapToEvent(clock: Clock): Event.Interaction? {
    if (payload.actions.isEmpty()) return null
    val action = payload.actions.first()
    val channel = Channel(payload.channel.id)
    val user = User(payload.user.id)
    val now = clock.instant()
    return when (InteractionType.fromId(action.actionId)) {
        InteractionType.BUTTON -> ButtonClicked(channel, now, user)
        InteractionType.OPTION -> OptionPicked(channel, now, user)
        InteractionType.TEXT_INPUT -> TextInput(channel, now, user, InputText(TODO("Implement modals")))
        InteractionType.RESPONSE -> null /*Block action cannot be triggered by response message*/
        InteractionType.UNKNOWN -> null
    }
}

fun BlockActionRequest.mapToConversationKey() = ConversationKey(
    channel = Channel(payload.channel.id),
    messageTs = payload.message.threadTs ?: payload.message.ts
)

fun BlockActionRequest.mapToInteractionKey(): InteractionKey? {
    if (payload.actions.isEmpty()) return null
    val action = payload.actions.first()
    val actionId = action.actionId ?: return null
    val valueId = action.value ?: action.selectedOption?.value ?: action?.text?.text ?: return null
    return InteractionKey(actionId = actionId, valueId = valueId)
}