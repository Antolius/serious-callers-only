package hr.from.josipantolis.seriouscallersonly.app.games

import hr.from.josipantolis.seriouscallersonly.dsl.CommandProtocol
import hr.from.josipantolis.seriouscallersonly.dsl.Replier

fun playCommandProtocol() = CommandProtocol(Replier {
    val txt = (it.text ?: "").toLowerCase().replace("\\s".toRegex(), "")
    if (txt.contains("tictactoe")) {
        return@Replier TicTacToeGame().renderBoard()
    }
    if (txt.contains("guess")) {
        return@Replier GuessingGame((1..10)).render()
    }
    return@Replier gameMenu()
})