package org.github.jantolis.seriouscallersonly.dsl

import org.junit.jupiter.api.Test

class DslTest {

    @Test
    fun `Should allow for interactive conversations`() {
        protocol {
            onNewMessage {
                reply(Content.Message::class) {
                    text("What's your favorite digit?")
                    selection("Pick one!", (0..9)
                            .map { "$it" }
                            .map { digit ->
                                SelectionOption(digit) {
                                    reply(Content.Message::class) {
                                        text("What's your favorite letter?")
                                        selection("Pick one!", ('a'..'z')
                                                .map { "$it" }
                                                .map { letter ->
                                                    SelectionOption(letter) {
                                                        reply(Content.Message::class) {
                                                            text("You picked $letter and $digit!")
                                                        }
                                                    }
                                                }
                                        )
                                    }
                                }
                            }
                    )
                }
            }
        }
    }

    @Test
    fun `should allow for guessing game`() {
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

}