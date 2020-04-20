package hr.from.josipantolis.seriouscallersonly.runtime.slack

import hr.from.josipantolis.seriouscallersonly.api.*
import hr.from.josipantolis.seriouscallersonly.api.Event.Interaction.*
import hr.from.josipantolis.seriouscallersonly.api.EventReplier.InteractionReplier.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.time.Instant

internal class ConversationTest {

    private val invalid = Errors(listOf("Invalid"))
    private val valid = Errors(listOf())
    private val reply = Reply.Message(listOf())

    @TestFactory
    fun `Should handle no validators`() = listOf(
        givenButtonEvent(),
        givenOptionEvent(),
        givenTextEvent(),
        givenResponseEvent()
    ).map { givenEvent ->
        dynamicTest("Should handle no validator with ${givenEvent::class} event") {
            // given
            val conv = givenConv()
            val key = givenKey()
            val liveInteraction = LiveInteraction(key, null, givenButtonReplier())

            // when
            var actual: Errors? = null
            runBlocking {
                conv.store(liveInteraction)
                actual = conv.validate(givenEvent, key)
            }

            // then
            then(actual).isNull()
        }
    }

    @TestFactory
    fun `Should handle button validators`() = listOf(
        Validator { _: ButtonClicked -> invalid } to givenButtonEvent() to invalid,
        Validator { _: ButtonClicked -> invalid } to givenOptionEvent() to null,
        Validator { _: ButtonClicked -> invalid } to givenTextEvent() to null,
        Validator { _: ButtonClicked -> invalid } to givenResponseEvent() to null,
        Validator { _: ButtonClicked -> valid } to givenButtonEvent() to valid,
        Validator { _: ButtonClicked -> valid } to givenOptionEvent() to null,
        Validator { _: ButtonClicked -> valid } to givenTextEvent() to null,
        Validator { _: ButtonClicked -> valid } to givenResponseEvent() to null
    ).map { (validatorAndEvent, expectedErrors) ->
        val (givenValidator, givenEvent) = validatorAndEvent
        dynamicTest("Should handle button validator with ${givenEvent::class} event and $expectedErrors result") {
            // given
            val conv = givenConv()
            val key = givenKey()
            val liveInteraction = LiveInteraction(key, givenValidator, givenButtonReplier())

            // when
            var actual: Errors? = null
            runBlocking {
                conv.store(liveInteraction)
                actual = conv.validate(givenEvent, key)
            }

            // then
            then(actual).isSameAs(expectedErrors)
        }
    }

    @TestFactory
    fun `Should handle options validators`() = listOf(
        Validator { _: OptionPicked -> invalid } to givenButtonEvent() to null,
        Validator { _: OptionPicked -> invalid } to givenOptionEvent() to invalid,
        Validator { _: OptionPicked -> invalid } to givenTextEvent() to null,
        Validator { _: OptionPicked -> invalid } to givenResponseEvent() to null,
        Validator { _: OptionPicked -> valid } to givenButtonEvent() to null,
        Validator { _: OptionPicked -> valid } to givenOptionEvent() to valid,
        Validator { _: OptionPicked -> valid } to givenTextEvent() to null,
        Validator { _: OptionPicked -> valid } to givenResponseEvent() to null
    ).map { (validatorAndEvent, expectedErrors) ->
        val (givenValidator, givenEvent) = validatorAndEvent
        dynamicTest("Should handle options validator with ${givenEvent::class} event and $expectedErrors result") {
            // given
            val conv = givenConv()
            val key = givenKey()
            val liveInteraction = LiveInteraction(key, givenValidator, givenOptionReplier())

            // when
            var actual: Errors? = null
            runBlocking {
                conv.store(liveInteraction)
                actual = conv.validate(givenEvent, key)
            }

            // then
            then(actual).isSameAs(expectedErrors)
        }
    }

    @TestFactory
    fun `Should handle text validators`() = listOf(
        Validator { _: TextInput -> invalid } to givenButtonEvent() to null,
        Validator { _: TextInput -> invalid } to givenOptionEvent() to null,
        Validator { _: TextInput -> invalid } to givenTextEvent() to invalid,
        Validator { _: TextInput -> invalid } to givenResponseEvent() to null,
        Validator { _: TextInput -> valid } to givenButtonEvent() to null,
        Validator { _: TextInput -> valid } to givenOptionEvent() to null,
        Validator { _: TextInput -> valid } to givenTextEvent() to valid,
        Validator { _: TextInput -> valid } to givenResponseEvent() to null
    ).map { (validatorAndEvent, expectedErrors) ->
        val (givenValidator, givenEvent) = validatorAndEvent
        dynamicTest("Should handle text validator with ${givenEvent::class} event and $expectedErrors result") {
            // given
            val conv = givenConv()
            val key = givenKey()
            val liveInteraction = LiveInteraction(key, givenValidator, givenTextReplier())

            // when
            var actual: Errors? = null
            runBlocking {
                conv.store(liveInteraction)
                actual = conv.validate(givenEvent, key)
            }

            // then
            then(actual).isSameAs(expectedErrors)
        }
    }

    @TestFactory
    fun `Should handle response validators`() = listOf(
        Validator { _: UserResponded -> invalid } to givenButtonEvent() to null,
        Validator { _: UserResponded -> invalid } to givenOptionEvent() to null,
        Validator { _: UserResponded -> invalid } to givenTextEvent() to null,
        Validator { _: UserResponded -> invalid } to givenResponseEvent() to invalid,
        Validator { _: UserResponded -> valid } to givenButtonEvent() to null,
        Validator { _: UserResponded -> valid } to givenOptionEvent() to null,
        Validator { _: UserResponded -> valid } to givenTextEvent() to null,
        Validator { _: UserResponded -> valid } to givenResponseEvent() to valid
    ).map { (validatorAndEvent, expectedErrors) ->
        val (givenValidator, givenEvent) = validatorAndEvent
        dynamicTest("Should handle response validator with ${givenEvent::class} event and $expectedErrors result") {
            // given
            val conv = givenConv()
            val key = givenKey()
            val liveInteraction = LiveInteraction(key, givenValidator, givenResponseReplier())

            // when
            var actual: Errors? = null
            runBlocking {
                conv.store(liveInteraction)
                actual = conv.validate(givenEvent, key)
            }

            // then
            then(actual).isSameAs(expectedErrors)
        }
    }

    @TestFactory
    fun `Should handle button repliers`() = listOf(
        ButtonClickedReplier { reply } to givenButtonEvent() to reply,
        ButtonClickedReplier { reply } to givenOptionEvent() to null,
        ButtonClickedReplier { reply } to givenTextEvent() to null,
        ButtonClickedReplier { reply } to givenResponseEvent() to null
    ).map { (replierAndEvent, expectedReply) ->
        val (givenReplier, givenEvent) = replierAndEvent
        dynamicTest("Should handle button replier with ${givenEvent::class} event and $expectedReply result") {
            // given
            val conv = givenConv()
            val key = givenKey()
            val liveInteraction = LiveInteraction(key, null, givenReplier)

            // when
            var actual: Reply? = null
            runBlocking {
                conv.store(liveInteraction)
                actual = conv.renderReply(givenEvent, key)
            }

            // then
            then(actual).isSameAs(expectedReply)
        }
    }

    @TestFactory
    fun `Should handle option repliers`() = listOf(
        OptionPickedReplier { reply } to givenButtonEvent() to null,
        OptionPickedReplier { reply } to givenOptionEvent() to reply,
        OptionPickedReplier { reply } to givenTextEvent() to null,
        OptionPickedReplier { reply } to givenResponseEvent() to null
    ).map { (replierAndEvent, expectedReply) ->
        val (givenReplier, givenEvent) = replierAndEvent
        dynamicTest("Should handle option replier with ${givenEvent::class} event and $expectedReply result") {
            // given
            val conv = givenConv()
            val key = givenKey()
            val liveInteraction = LiveInteraction(key, null, givenReplier)

            // when
            var actual: Reply? = null
            runBlocking {
                conv.store(liveInteraction)
                actual = conv.renderReply(givenEvent, key)
            }

            // then
            then(actual).isSameAs(expectedReply)
        }
    }

    @TestFactory
    fun `Should handle text repliers`() = listOf(
        TextInputReplier { reply } to givenButtonEvent() to null,
        TextInputReplier { reply } to givenOptionEvent() to null,
        TextInputReplier { reply } to givenTextEvent() to reply,
        TextInputReplier { reply } to givenResponseEvent() to null
    ).map { (replierAndEvent, expectedReply) ->
        val (givenReplier, givenEvent) = replierAndEvent
        dynamicTest("Should handle option replier with ${givenEvent::class} event and $expectedReply result") {
            // given
            val conv = givenConv()
            val key = givenKey()
            val liveInteraction = LiveInteraction(key, null, givenReplier)

            // when
            var actual: Reply? = null
            runBlocking {
                conv.store(liveInteraction)
                actual = conv.renderReply(givenEvent, key)
            }

            // then
            then(actual).isSameAs(expectedReply)
        }
    }

    @TestFactory
    fun `Should handle response repliers`() = listOf(
        UserRespondedReplier { reply } to givenButtonEvent() to null,
        UserRespondedReplier { reply } to givenOptionEvent() to null,
        UserRespondedReplier { reply } to givenTextEvent() to null,
        UserRespondedReplier { reply } to givenResponseEvent() to reply
    ).map { (replierAndEvent, expectedReply) ->
        val (givenReplier, givenEvent) = replierAndEvent
        dynamicTest("Should handle option replier with ${givenEvent::class} event and $expectedReply result") {
            // given
            val conv = givenConv()
            val key = givenKey()
            val liveInteraction = LiveInteraction(key, null, givenReplier)

            // when
            var actual: Reply? = null
            runBlocking {
                conv.store(liveInteraction)
                actual = conv.renderReply(givenEvent, key)
            }

            // then
            then(actual).isSameAs(expectedReply)
        }
    }

    private fun givenButtonEvent() = ButtonClicked(
        channel = Channel("channel"),
        happenedAt = Instant.now(),
        clickedBy = User("user")
    )

    private fun givenOptionEvent() = OptionPicked(
        channel = Channel("channel"),
        happenedAt = Instant.now(),
        pickedBy = User("user")
    )

    private fun givenTextEvent() = TextInput(
        channel = Channel("channel"),
        happenedAt = Instant.now(),
        author = User("user"),
        text = InputText("text")
    )

    private fun givenResponseEvent() = UserResponded(
        channel = Channel("channel"),
        happenedAt = Instant.now(),
        author = User("user"),
        content = MessageText("text")
    )

    private fun givenKey() = InteractionKey("id", "id")

    private fun givenConv() = Conversation(
        channel = Channel("channel")
    )

    private fun givenButtonReplier() = ButtonClickedReplier { Reply.Message(listOf()) }
    private fun givenOptionReplier() = OptionPickedReplier { Reply.Message(listOf()) }
    private fun givenTextReplier() = TextInputReplier { Reply.Message(listOf()) }
    private fun givenResponseReplier() = UserRespondedReplier { Reply.Message(listOf()) }

}