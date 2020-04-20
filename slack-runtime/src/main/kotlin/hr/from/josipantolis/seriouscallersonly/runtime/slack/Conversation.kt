package hr.from.josipantolis.seriouscallersonly.runtime.slack

import hr.from.josipantolis.seriouscallersonly.api.*
import hr.from.josipantolis.seriouscallersonly.api.Event.Interaction.*
import hr.from.josipantolis.seriouscallersonly.runtime.repo.MapRepo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

class Conversation(
    val channel: Channel,
    val thread: Thread? = null,
    var triggerId: String? = null,
    var updateableMessageTs: String? = null,
    var messageTsToDelete: String? = null,
    var responseReplier: EventReplier.InteractionReplier.UserRespondedReplier? = null
) {
    private val mtx = Mutex()

    var key: ConversationKey? = null
    val buttonInteractions = MapRepo<InteractionKey, LiveInteraction<ButtonClicked>> { it.key }
    val optionInteractions = MapRepo<InteractionKey, LiveInteraction<OptionPicked>> { it.key }
    val inputInteractions = MapRepo<InteractionKey, LiveInteraction<TextInput>> { it.key }
    val responseInteractions = MapRepo<InteractionKey, LiveInteraction<UserResponded>> { it.key }

    suspend fun <I : Event.Interaction> validate(interaction: I, key: InteractionKey): Errors? = mtx.withLock {
        when (interaction) {
            is ButtonClicked -> buttonInteractions.find(key)?.validator?.check?.invoke(interaction)
            is OptionPicked -> optionInteractions.find(key)?.validator?.check?.invoke(interaction)
            is TextInput -> inputInteractions.find(key)?.validator?.check?.invoke(interaction)
            is UserResponded -> responseInteractions.find(key)?.validator?.check?.invoke(interaction)
            else -> null
        }
    }

    suspend fun <I : Event.Interaction> renderReply(interaction: I, key: InteractionKey): Reply? = mtx.withLock {
        when (interaction) {
            is ButtonClicked -> buttonInteractions.find(key)?.replier?.cb?.invoke(interaction)
            is OptionPicked -> optionInteractions.find(key)?.replier?.cb?.invoke(interaction)
            is TextInput -> inputInteractions.find(key)?.replier?.cb?.invoke(interaction)
            is UserResponded -> responseInteractions.find(key)?.replier?.cb?.invoke(interaction)
            else -> null
        }
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

    suspend fun clearLiveInteractionsFor(actionId: String) = mtx.withLock {
        buttonInteractions.all()
            .map { (key, _) -> key }
            .filter { it.actionId == actionId }
            .forEach { buttonInteractions.remove(it) }
        optionInteractions.all()
            .map { (key, _) -> key }
            .filter { it.actionId == actionId }
            .forEach { optionInteractions.remove(it) }
        inputInteractions.all()
            .map { (key, _) -> key }
            .filter { it.actionId == actionId }
            .forEach { inputInteractions.remove(it) }
        responseInteractions.all()
            .map { (key, _) -> key }
            .filter { it.actionId == actionId }
            .forEach { responseInteractions.remove(it) }
    }

    suspend fun clearAllLiveInteractions() = mtx.withLock {
        buttonInteractions.clear()
        optionInteractions.clear()
        inputInteractions.clear()
        responseInteractions.clear()
    }

    suspend fun isCompleted() = mtx.withLock {
        responseReplier == null
                && buttonInteractions.isEmpty()
                && optionInteractions.isEmpty()
                && inputInteractions.isEmpty()
                && responseInteractions.isEmpty()
    }

}