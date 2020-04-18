package hr.from.josipantolis.seriouscallersonly.api

import hr.from.josipantolis.seriouscallersonly.api.script.CallScript
import org.junit.jupiter.api.Test

class MessageBuildingTest {

    @Test
    fun `Should be able to build rich messages`() {
        val script: CallScript.() -> Unit = {
            this register commandProtocol("/show-off".cmd) {
                replyPublicly {
                    +section("You can send plain text...".txt)
                    +section("and markdown with **bold** and _italic_!".md)
                    +divider
                    +section("Plain text section with 2 column table:".txt) {
                        +"Header `#1`".md
                        +"Header `#2`".md
                        +"Row 1:1".txt
                        +"Row 1:2".txt
                        +"Row 2:1".txt
                        +"Row 2:2".txt
                    }
                    +divider
                    +section("`Markdown` section with a fancy accessory!".md) {
                        accessory = image("https://bit.ly/2VifV0m".url to "A cat image. Probably.")
                    }
                    +section("You can also use a button as accessory:".txt) {
                        accessory = button("Click me!".txt) {
                            replyPublicly {
                                +section("Tnx for clicking that button!".txt)
                            }
                        }
                    }
                    +section("Or a drop-down list of options:".txt) {
                        accessory = select("Pick one!".txt) {
                            +option("Option A".txt) {
                                replyPublicly {
                                    +section("A is the **best** choice!".md)
                                }
                            }
                            +option("Option B".txt) {
                                replyPublicly {
                                    +section("B is the **best** choice!".md)
                                }
                            }
                        }
                    }
                    +section("Or even a triple dot menu! Check it out:".txt) {
                        accessory = overflow {
                            +option("1st menu option".txt) {
                                replyPublicly {
                                    +section(":thumbsup:".md)
                                }
                            }
                            +option("2nd option".txt) {
                                replyPublicly {
                                    +section(":thumbsdown:".md)
                                }
                            }
                        }
                    }
                    +divider
                    +actions {
                        +button.primary("Yes.".txt) {
                            replyPublicly {
                                +section("Future is bright!".txt)
                            }
                        }
                        +button.danger("No.".txt) {
                            replyPublicly {
                                +section("Sorry to hear this.".txt)
                            }
                        }
                        +button("Maybe?".txt) {
                            replyPublicly {
                                +section("What am I to think of that?".txt)
                            }
                        }
                        +overflow {
                            +option("I don't know.".txt) {
                                replyPublicly {
                                    +section("Oh, I see.".txt)
                                }
                            }
                            +option("Can you repeat the question?".txt) {
                                replyPublicly {
                                    +section("You're not the boss of me now.".txt)
                                }
                            }

                        }
                    }
                    +divider
                    +context {
                        +"Some _contextual_ text.".md
                        +"(Doesn't need to be markdown.)".txt
                        +image("https://bit.ly/2VjZTmQ".url to "Context is king")
                    }
                    +divider
                    +image("https://bit.ly/2xu5i1H".url to "That's enough typing for now.")
                }.andThen {
                    replyPublicly {
                        +section("Followup Message!".txt)
                    }
                }.onResponse { response ->
                    replyPrivatelyTo(response.author) {
                        +section("Hey ${response.author.mention}, tnx for responding!".md)
                        +section((">${response.content.txt}\n That sure sounds interesting.").md)
                    }
                }
            }
        }
    }
}
