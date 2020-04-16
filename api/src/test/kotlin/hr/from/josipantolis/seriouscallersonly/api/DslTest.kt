package hr.from.josipantolis.seriouscallersonly.api

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
        Bot(
            channelProtocols = mapOf(
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
                                                                    Reply.Message(
                                                                        blocks = listOf(
                                                                            Block.Section(
                                                                                text = Element.Text.Plain("You picked $digit and $letter!")
                                                                            )
                                                                        )
                                                                    )
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
            )
        )
    }
}
