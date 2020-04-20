package hr.from.josipantolis.seriouscallersonly.app.actuator

import hr.from.josipantolis.seriouscallersonly.runtime.repo.Repo
import hr.from.josipantolis.seriouscallersonly.runtime.slack.Conversation
import hr.from.josipantolis.seriouscallersonly.runtime.slack.ConversationKey
import kotlinx.coroutines.runBlocking
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation

@Endpoint(id = "conversations")
open class ConversationsEndpoint(private val conversations: Repo<ConversationKey, Conversation>) {

    @ReadOperation
    fun conversations() = runBlocking {
        mapOf(
            "conversations" to conversations.all()
                .map { (key, _) -> key }
                .map {
                    mapOf(
                        "channel" to it.channel.id,
                        "thread" to it.messageTs
                    )
                }
        )
    }

}