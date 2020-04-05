package org.github.jantolis.seriouscallersonly.app

import org.github.jantolis.seriouscallersonly.dsl.Content
import org.github.jantolis.seriouscallersonly.dsl.SelectionOption
import org.github.jantolis.seriouscallersonly.dsl.protocol

fun gameProtocol() =
        protocol {
            onNewMessage {
                val game = Game((1..10))
                reply(Content.Message::class) {
                    text("I picked a number between ${game.options.first()} and ${game.options.last()}")
                    text("Can you guess which one it is?")
                    selection("Pick a number!", game.options.map { toSelectOption(it, game) })
                }
            }
        }

class Game(range: IntRange) {
    val options = range.toMutableList()
    val answer = options.random()
}

private fun toSelectOption(guess: Int, game: Game): SelectionOption = SelectionOption("$guess") {
    reply(Content.Message::class) {
        if (guess == game.answer) {
            text("Correct!!!")
        } else {
            game.options.remove(guess)
            text("Wrong! Guess again!")
            selection("Pick another number!", game.options.map { toSelectOption(it, game) })
        }
    }
}