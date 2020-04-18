package hr.from.josipantolis.seriouscallersonly.runtime.slack

import hr.from.josipantolis.seriouscallersonly.api.*
import hr.from.josipantolis.seriouscallersonly.api.Event.Interaction.*
import hr.from.josipantolis.seriouscallersonly.runtime.repository.ConcurrentRepo
import hr.from.josipantolis.seriouscallersonly.runtime.repository.MapRepo
import hr.from.josipantolis.seriouscallersonly.runtime.repository.Repo

data class InteractionKey(
    val actionId: String,
    val valueId: String
)

class LiveInteraction<in I : Event.Interaction>(
    val key: InteractionKey,
    val validator: Validator<I>? = null,
    val replier: EventReplier.InteractionReplier<I>
) {
    lateinit var conversation: Conversation
}

data class ConversationKey(
    val channel: Channel,
    val messageTs: String
)

private typealias InteractionRepo<I> = Repo<InteractionKey, LiveInteraction<I>>

class Conversation(
    val channel: Channel,
    val user: User? = null,
    val thread: Thread? = null,
    var triggerId: String? = null,
    var updateableMessageTs: String? = null,
    var messageTsToDelete: String? = null
) {

    var key: ConversationKey? = null
    val buttonInteractions = ConcurrentRepo<InteractionKey, LiveInteraction<ButtonClicked>>(MapRepo { it.key })
    val optionInteractions = ConcurrentRepo<InteractionKey, LiveInteraction<OptionPicked>>(MapRepo { it.key })
    val inputInteractions = ConcurrentRepo<InteractionKey, LiveInteraction<TextInput>>(MapRepo { it.key })
    val responseInteractions = ConcurrentRepo<InteractionKey, LiveInteraction<UserResponded>>(MapRepo { it.key })

    suspend fun <I : Event.Interaction> validate(interaction: I, key: InteractionKey): Errors? =
        when (interaction) {
            is ButtonClicked -> buttonInteractions.find(key)?.validator?.check?.invoke(interaction)
            is OptionPicked -> optionInteractions.find(key)?.validator?.check?.invoke(interaction)
            is TextInput -> inputInteractions.find(key)?.validator?.check?.invoke(interaction)
            is UserResponded -> responseInteractions.find(key)?.validator?.check?.invoke(interaction)
            else -> null
        }

    suspend fun <I : Event.Interaction> renderReply(interaction: I, key: InteractionKey): Reply? =
        when (interaction) {
            is ButtonClicked -> buttonInteractions.find(key)?.replier?.cb?.invoke(interaction)
            is OptionPicked -> optionInteractions.find(key)?.replier?.cb?.invoke(interaction)
            is TextInput -> inputInteractions.find(key)?.replier?.cb?.invoke(interaction)
            is UserResponded -> responseInteractions.find(key)?.replier?.cb?.invoke(interaction)
            else -> null
        }

    suspend inline fun <reified I : Event.Interaction> store(liveInteraction: LiveInteraction<I>) {
        liveInteraction.conversation = this
        when (I::class) {
            ButtonClicked::class -> buttonInteractions.store(liveInteraction as LiveInteraction<ButtonClicked>)
            OptionPicked::class -> optionInteractions.store(liveInteraction as LiveInteraction<OptionPicked>)
            TextInput::class -> inputInteractions.store(liveInteraction as LiveInteraction<TextInput>)
            UserResponded::class -> responseInteractions.store(liveInteraction as LiveInteraction<UserResponded>)
        }
    }

    suspend fun clearLiveInteractions() {
        buttonInteractions.clear()
        optionInteractions.clear()
        inputInteractions.clear()
        responseInteractions.clear()
    }

}