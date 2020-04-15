package org.github.jantolis.seriouscallersonly.app.games

import org.github.jantolis.seriouscallersonly.dsl.*

fun playCommandProtocol() = CommandProtocol(Replier {
    val txt = (it.text ?: "").toLowerCase().replace("\\s".toRegex(), "")
    if (txt.contains("tictactoe")) {
        return@Replier TicTacToeGame().renderBoard()
    }
    if (txt.contains("guessing")) {
        return@Replier GuessingGame((1..10)).render()
    }
    val gamePrompt = Block.Section(text = Element.Text.Plain("Which game would you like to play?"))
    return@Replier Reply.Message(blocks = listOf(
            gamePrompt,
            Block.Actions(elements = listOf(
                    Element.Button(
                            text = Element.Text.Plain("Tic Tac Toe"),
                            onClick = Replier { interaction ->
                                Reply.ReplacementMessage(
                                        blocks = listOf(
                                                gamePrompt,
                                                Block.Section(text = Element.Text.Markdown("${interaction.actor.mention} picked Tic Tac Toe!"))
                                        ),
                                        andThen = VoidReplier { TicTacToeGame().renderBoard() }
                                )
                            }
                    ),
                    Element.Button(
                            text = Element.Text.Plain("Guessing game"),
                            onClick = Replier { interaction ->
                                Reply.ReplacementMessage(
                                        blocks = listOf(
                                                gamePrompt,
                                                Block.Section(text = Element.Text.Markdown("${interaction.actor.mention} picked Guessing game!"))
                                        ),
                                        andThen = VoidReplier { GuessingGame((1..10)).render() }
                                )
                            }
                    )
            ))
    ))
})