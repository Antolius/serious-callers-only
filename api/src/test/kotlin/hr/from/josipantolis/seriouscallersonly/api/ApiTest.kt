package hr.from.josipantolis.seriouscallersonly.api

import hr.from.josipantolis.seriouscallersonly.api.EventReplier.InteractionReplier.OptionPickedReplier
import hr.from.josipantolis.seriouscallersonly.api.EventReplier.PublicMessagePostedReplier
import org.junit.jupiter.api.Test

class ApiTest {

    @Test
    fun `Should be able to build bot directly using API models`() {
        Bot(
            channelProtocols = mapOf(
                Channel("made-up") to ChannelProtocol(
                    channel = Channel("some-channel-id"),
                    onPublicMessage = PublicMessagePostedReplier {
                        Reply.Message(blocks = listOf(
                            Block.Section(
                                text = Element.Text.Plain("What's your favorite digit?"),
                                accessory = Element.Select(
                                    placeholder = Element.Text.Plain("Pick one!"),
                                    options = (0..9).map { digit ->
                                        Option(
                                            text = Element.Text.Plain("$digit"),
                                            onPick = OptionPickedReplier {
                                                Reply.Message(blocks = listOf(
                                                    Block.Section(
                                                        text = Element.Text.Plain("And what's your favorite letter?"),
                                                        accessory = Element.Select(
                                                            placeholder = Element.Text.Plain("Pick one!"),
                                                            options = ('a'..'z').map { letter ->
                                                                Option(
                                                                    text = Element.Text.Plain("$letter"),
                                                                    onPick = OptionPickedReplier {
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
                    }
                )
            )
        )
    }
}
