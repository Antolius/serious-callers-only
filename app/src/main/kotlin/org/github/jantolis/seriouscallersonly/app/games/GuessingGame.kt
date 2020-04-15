package org.github.jantolis.seriouscallersonly.app.games

import org.github.jantolis.seriouscallersonly.dsl.*

internal class GuessingGame(range: IntRange) {
    private val options = range.toMutableList()
    private val answer = options.random()

    fun render() = Reply.Message(blocks = listOf(
            Block.Section(text = Element.Text.Plain("I picked a number between ${options.first()} and ${options.last()}")),
            Block.Section(
                    text = Element.Text.Plain("Can you guess which one it is?"),
                    accessory = Element.Select(
                            placeholder = Element.Text.Plain("Pick a number!"),
                            options = options.map { toOption(it) }
                    )
            )
    ))

    private fun toOption(guess: Int): Option {
        return Option(
                text = Element.Text.Plain("$guess"),
                onSelect = Replier {
                    if (guess == answer) {
                        Reply.Message(blocks = listOf(Block.Section(text = Element.Text.Plain("Correct!!!"))))
                    } else {
                        options.remove(guess)
                        Reply.Message(blocks = listOf(
                                Block.Section(
                                        text = Element.Text.Plain("Wrong! Guess again!"),
                                        accessory = Element.Select(
                                                placeholder = Element.Text.Plain("Pick another number!"),
                                                options = options.map { toOption(it) }
                                        )
                                )
                        ))
                    }
                }
        )
    }

}
