register(
    ChannelProtocol(
        channel = Channel("C012340RJ91"),
        timerProtocol = TimerProtocol(
            cron = "* 0/5 * * * *",
            onTimer = EventReplier.TimerReplier {
                Reply.Message(
                    blocks = listOf(
                        Block.Section(
                            text = Element.Text.Markdown(
                                "Hello ${User.specialMention.here}!"
                            )
                        )
                    )
                )
            }
        )
    )
)
