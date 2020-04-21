register(
    CommandProtocol(
        command = Command("/pick-a-book-imperative"),
        onCommandInvoked = EventReplier.CommandInvokedReplier {
            Reply.Message(
                blocks = listOf(
                    Block.Section(
                        text = Element.Text.Plain("Looking for fiction or a technical book?"),
                        accessory = Element.Select(
                            placeholder = Element.Text.Plain("Pick one!"),
                            options = listOf(
                                Option(
                                    text = Element.Text.Plain("Technical"),
                                    onPick = EventReplier.InteractionReplier.OptionPickedReplier {
                                        Reply.Message(
                                            blocks = listOf(
                                                Block.Section(
                                                    text = Element.Text.Markdown("_The Pragmatic Programmer_ is a classic.")
                                                ),
                                                Block.Context(
                                                    elements = listOf(
                                                        Element.Text.Plain("It's written by Andrew Hunt")
                                                    )
                                                )
                                            )
                                        )
                                    }
                                ),
                                Option(
                                    text = Element.Text.Plain("Fiction"),
                                    onPick = EventReplier.InteractionReplier.OptionPickedReplier {
                                        Reply.Message(
                                            blocks = listOf(
                                                Block.Section(
                                                    text = Element.Text.Markdown("Have particular genre in mind?"),
                                                    accessory = Element.Select(
                                                        placeholder = Element.Text.Plain("Pick one!"),
                                                        options = listOf(
                                                            Option(
                                                                text = Element.Text.Plain("Science Fiction"),
                                                                onPick = EventReplier.InteractionReplier.OptionPickedReplier {
                                                                    Reply.Message(
                                                                        blocks = listOf(
                                                                            Block.Section(
                                                                                text = Element.Text.Markdown("I particularly enjoy _Excession_.")
                                                                            ),
                                                                            Block.Context(
                                                                                elements = listOf(
                                                                                    Element.Text.Plain("It's written by Iain M. Banks")
                                                                                )
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                            ),
                                                            Option(
                                                                text = Element.Text.Plain("Fantasy"),
                                                                onPick = EventReplier.InteractionReplier.OptionPickedReplier {
                                                                    Reply.Message(
                                                                        blocks = listOf(
                                                                            Block.Section(
                                                                                text = Element.Text.Markdown("_Foundryside_ is an absolute treasure!")
                                                                            ),
                                                                            Block.Context(
                                                                                elements = listOf(
                                                                                    Element.Text.Plain("It's written by Robert Jackson Bennett")
                                                                                )
                                                                            )
                                                                        )
                                                                    )
                                                                }
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    }
                                )
                            )
                        )
                    )
                )
            )
        }
    )
)