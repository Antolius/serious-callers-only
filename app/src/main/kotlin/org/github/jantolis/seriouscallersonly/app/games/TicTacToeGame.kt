package org.github.jantolis.seriouscallersonly.app.games

import org.github.jantolis.seriouscallersonly.dsl.*

internal class TicTacToeGame {

    enum class Cell(
            val text: String,
            val style: ButtonStyle,
            val validator: ((Interaction) -> Errors)?
    ) {
        EMPTY("_", ButtonStyle.DEFAULT, null),
        O("O", ButtonStyle.PRIMARY, { listOf(ERR.MSG) }),
        X("X", ButtonStyle.DANGER, { listOf(ERR.MSG) });

        object ERR {
            const val MSG = "This cell is already selected. Pick another one!"
        }
    }

    private val board = (0..2).flatMap { x -> (0..2).map { y -> x to y } }
            .associateWith { Cell.EMPTY }
            .toMutableMap()

    private var winningLine: List<Pair<Int, Int>> = listOf()

    fun renderBoard(): Reply.Message = Reply.Message(blocks = renderBlocks())

    private fun reRenderBoard(andThen: VoidReplier? = null): Reply = Reply.ReplacementMessage(
            blocks = renderBlocks(),
            andThen = andThen
    )

    private fun renderBlocks() = (0..2).map { x ->
        Block.Actions(elements = (0..2).map { y ->
            renderCell(x to y)
        })
    }

    private fun renderCell(xy: Pair<Int, Int>) = with(board.getValue(xy)) {
        Element.Button(
                text = Element.Text.Plain(text = if (winningLine.contains(xy)) {
                    "[$text]"
                } else {
                    text
                }),
                style = style,
                validate = validator?.let { Validator(it) },
                onClick = Replier {
                    board[xy] = Cell.O
                    reDetectWinningLine()
                    if (winningLine.isNotEmpty()) {
                        return@Replier reRenderBoard(VoidReplier { renderVictoryMsg() })
                    }
                    makeBotTurn()
                    reDetectWinningLine()
                    if (winningLine.isNotEmpty()) {
                        return@Replier reRenderBoard(VoidReplier { renderVictoryMsg() })
                    }
                    reRenderBoard()
                }
        )
    }

    private fun renderVictoryMsg() = Reply.Message(blocks = listOf(
            Block.Section(
                    text = Element.Text.Markdown("""
                                Game over! :tada: :confetti_ball: :tada:
                                The winner is: *${board[winningLine.first()]?.text}* !
                            """.trimIndent()
                    ),
                    accessory = Element.Button(
                            text = Element.Text.Plain("Play another game!"),
                            onClick = Replier { gameMenu() }
                    )
            )
    ))

    private fun makeBotTurn() {
        val availableCells = board.entries
                .filter { it.value == Cell.EMPTY }
                .map { it.key }
        if (availableCells.isNotEmpty()) {
            availableCells.random().also { board[it] = Cell.X }
        }
    }

    private fun reDetectWinningLine() {
        winningLine = lines.asSequence()
                .find { victoryRows.contains(it.map { xy -> board[xy] }) } ?: listOf()
    }

    private val lines = listOf(
            listOf(0 to 0, 1 to 1, 2 to 2),
            listOf(0 to 2, 1 to 1, 2 to 0),
            listOf(0 to 0, 0 to 1, 0 to 2),
            listOf(1 to 0, 1 to 1, 1 to 2),
            listOf(2 to 0, 2 to 1, 2 to 2),
            listOf(0 to 0, 1 to 0, 2 to 0),
            listOf(0 to 1, 1 to 1, 2 to 1),
            listOf(0 to 2, 1 to 2, 2 to 2)
    )

    private val victoryRows = setOf(
            listOf(Cell.X, Cell.X, Cell.X),
            listOf(Cell.O, Cell.O, Cell.O)
    )

}
