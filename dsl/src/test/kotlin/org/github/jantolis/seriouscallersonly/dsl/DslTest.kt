package org.github.jantolis.seriouscallersonly.dsl

import org.junit.jupiter.api.Test

class DslTest {

    @Test
    fun `Should allow for interactive conversations`() {
//        onNewChannelMessage { message ->
//            replyMessage {
//                += section {
//                    text = plainText("What's your favorite digit?")
//                    accessory = select {
//                        placeholder = plainText("Pick one!")
//                        options = (0..9).map { digit -> option {
//                            text = plainText("$digit")
//                            onSelect = {
//                                replyMessage {
//                                    += section {
//                                        text = plainText("What's your favorite letter?")
//                                        accessory = select {
//                                            placeholder = plainText("Pick one!")
//                                            options = ('a'..'z').map { letter -> option {
//                                                text = plainText("$digit")
//                                                onSelect = {
//                                                    replyMessage {
//                                                        += section {
//                                                            text = plainText("You picked $digit & $letter")
//                                                        }
//                                                    }
//                                                }
//                                            }}
//                                        }
//                                    }
//                                }
//                            }
//                        }}
//                    }
//                }
//            }
//        }
        Bot(channelProtocols = mapOf(
                Channel("made-up") to ChannelProtocol(onNewChannelMessage = Replier {
                    Reply.Message(blocks = listOf(
                            Block.Section(
                                    text = Element.Text.Plain("What's your favorite digit?"),
                                    accessory = Element.Select(
                                            placeholder = Element.Text.Plain("Pick one!"),
                                            options = (0..9).map { digit ->
                                                Option(
                                                        text = Element.Text.Plain("$digit"),
                                                        onSelect = Replier {
                                                            Reply.Message(blocks = listOf(
                                                                    Block.Section(
                                                                            text = Element.Text.Plain("And what's your favorite letter?"),
                                                                            accessory = Element.Select(
                                                                                    placeholder = Element.Text.Plain("Pick one!"),
                                                                                    options = ('a'..'z').map { letter ->
                                                                                        Option(
                                                                                                text = Element.Text.Plain("$letter"),
                                                                                                onSelect = Replier {
                                                                                                    Reply.Message(blocks = listOf(
                                                                                                            Block.Section(
                                                                                                                    text = Element.Text.Plain("You picked $digit and $letter!")
                                                                                                            )
                                                                                                    ))
                                                                                                }
                                                                                        )
                                                                                    }
                                                                            )
                                                                    )
                                                            ))
                                                        }
                                                )
                                            }
                                    )
                            )
                    ))
                })
        ))
    }

    @Test
    fun `should allow for guessing game`() {
        Bot(channelProtocols = mapOf(
                Channel("game-channel") to ChannelProtocol(onNewChannelMessage = Replier {
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
        ))
    }

    class Game(range: IntRange) {
        val options = range.toMutableList()
        val answer = options.random()
    }

    private fun toOption(guess: Int, game: Game): Option {
        return Option(
                text = Element.Text.Plain("$guess"),
                onSelect = Replier {
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
}
