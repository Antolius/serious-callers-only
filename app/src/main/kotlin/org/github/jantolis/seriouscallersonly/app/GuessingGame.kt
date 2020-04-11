package org.github.jantolis.seriouscallersonly.app

import org.github.jantolis.seriouscallersonly.dsl.*

fun gameProtocol() = ChannelProtocol(onNewChannelMessage = Replier {
    val game = Game((1..10))
    Reply.Message(blocks = listOf(
            Block.Section(text = Element.Text.Plain("I picked a number between ${game.options.first()} and ${game.options.last()}")),
            Block.Section(
                    text = Element.Text.Plain("Can you guess which one it is?"),
                    accessory = Element.Select(
                            placeholder = Element.Text.Plain("Pick a number!"),
                            options = game.options.map { toOption(it, game) }
                    )
            )
    ))
})

private class Game(range: IntRange) {
    val options = range.toMutableList()
    val answer = options.random()
}

private fun toOption(guess: Int, game: Game): Option {
    return Option(
            text = Element.Text.Plain("$guess"),
            onSelect = VoidReplier {
                if (guess == game.answer) {
                    Reply.Message(blocks = listOf(Block.Section(text = Element.Text.Plain("Correct!!!"))))
                } else {
                    game.options.remove(guess)
                    Reply.Message(blocks = listOf(
                            Block.Section(
                                    text = Element.Text.Plain("Wrong! Guess again!"),
                                    accessory = Element.Select(
                                            placeholder = Element.Text.Plain("Pick another number!"),
                                            options = game.options.map { toOption(it, game) }
                                    )
                            )
                    ))
                }
            }
    )
}