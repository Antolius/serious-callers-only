this register botJoinedChannelReplier { (channel, _) ->
    replyPublicly {
        +section("Hello there!".txt)
        +context {
            +"Posted in ${channel.mention}".md
            +"Channel id: `${channel.id}`".md
        }
    }
}

this register channelProtocol("C01238P5SHK".channel) {
    onUserJoined { (channel, _, user) ->
        replyPrivatelyTo(user) {
            +section("Hello there!".txt)
            +context {
                +"Posted in ${channel.mention}".md
                +"Channel id: `${channel.id}`".md
                +"Greeting for ${user.mention}".md
                +"User id: `${user.id}`".md
            }
        }.andThen { replyPublicly {
            +section("${User.specialMention.here} say hello to ${user.mention}!".md)
        } }
    }

    onPublicMessage { (channel, _, author, msgTxt) ->
        replyPublicly {
            +section("Hello there!".txt)
            +context {
                +"Posted in ${channel.mention}".md
                +"Channel id: `${channel.id}`".md
                +"Responding to message by ${author.mention}".md
                +"User id: `${author.id}`".md
                +"Original message: \n>${msgTxt.txt}".md
            }
        }.onResponse { (channel, _, author, msgTxt) ->
            replyPublicly {
                +section("Hello there!".txt) {
                    accessory = button("Enough already".txt) { (channel, _, clickedBy) ->
                        replyPublicly {
                            +section("No more hellos or you! Got it.".txt)
                            +context {
                                +"Posted as a button reply in ${channel.mention}".md
                                +"Channel id: `${channel.id}`".md
                                +"Responding to click by ${clickedBy.mention}".md
                                +"User id: `${clickedBy.id}`".md
                            }
                        }
                    }
                }
                +context {
                    +"Posted in thread in ${channel.mention}".md
                    +"Channel id: `${channel.id}`".md
                    +"Responding to threaded message by ${author.mention}".md
                    +"User id: `${author.id}`".md
                    +"Original message: \n>${msgTxt.txt}".md
                }
            }
        }
    }
}

this register privateMessageReplier { (channel, _, author, msgTxt) ->
    replyPublicly {
        +section("Hello there!".txt)
        +context {
            +"Posted in private channel ${channel.mention}".md
            +"Channel id: `${channel.id}`".md
            +"Responding to for ${author.mention}".md
            +"User id: `${author.id}`".md
            +"Original message: \n>${msgTxt.txt}".md
        }
    }
}
