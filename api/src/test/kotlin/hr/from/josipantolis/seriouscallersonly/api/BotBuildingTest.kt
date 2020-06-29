package hr.from.josipantolis.seriouscallersonly.api

import hr.from.josipantolis.seriouscallersonly.api.script.CallScript
import org.junit.jupiter.api.Test

class BotBuildingTest {

    @Test
    fun `Should be able to build bot with all features`() {
        val script: CallScript.() -> Unit = {
            this register channelProtocol("channel-id".channel) {
                onTimer("* 0/5 * * * *") { timeout ->
                    replyPublicly {
                        +section("Current time is ${timeout.happenedAt}.".txt)
                    }
                }
                onPublicMessage { message ->
                    replyPublicly {
                        +section("Tnx for posting ${message.content.txt}".txt)
                    }
                }
                onUserJoined { join ->
                    replyPrivatelyTo(join.user) {
                        +section("Tnx for joining!".txt)
                    }
                }
            }
            this register commandProtocol("/whoami".cmd) { call ->
                replyPrivatelyTo(call.invoker) {
                    +section("You are ${call.invoker.mention}".md)
                }
            }
            this register privateMessageReplier { message ->
                replyPublicly {
                    +section("Tnx for messaging me ${message.author.mention}".md)
                }
            }
            this register homeTabVisitReplier { visit ->
                replyPrivatelyTo(visit.visitor) {
                    +section("Welcome to my home!".txt)
                }
            }
            this register botJoinedChannelReplier { join ->
                replyPublicly {
                    +section("${join.channel.mention} is the best channel ever!".md)
                }
            }
            this register privateMessageReplier { message ->
                openModal("Title".txt) {
                    +"Single line input" to textInput {
                        placeholder = "Blah blah blah".txt
                        initialValue = "Aha!"
                        validate = { value: String -> value.isNotBlank() }
                    }
                    +"Multi line input" to multiLineTextInput {
                        placeholder = "Blah blah blah".txt
                        initialValue = "A\nh\na\n!"
                        validate = { value: String -> value.isNotBlank() }
                    }
                }.onSubmit { payload: Map<String, String> ->
                    payload["Single line input"]
                    payload["Multi line input"]
                    
                }
            }
        }
    }
}
