package scripts

this register channelProtocol("channel-id".channel) {
    onTimer("* 0/5 * * * *") { timeout ->
        replyPublicly {
            +section("Current time is ${timeout.happenedAt}.".txt)
        }
    }
    onPublicMessage { message ->
        replyPublicly {
            +section("Tnx for posting ${message.content.txt}".txt)
        }
    }
    onUserJoined { join ->
        replyPrivatelyTo(join.user) {
            +section("Tnx for joining!".txt)
        }
    }
}
this register commandProtocol("/whoami".cmd) { call ->
    replyPrivatelyTo(call.invoker) {
        +section("You are ${call.invoker.mention}".md)
    }
}
this register privateMessageReplier { message ->
    replyPublicly {
        +section("Tnx for messaging me ${message.author.mention}".md)
    }
}
this register homeTabVisitReplier { visit ->
    replyPrivatelyTo(visit.visitor) {
        +section("Welcome to my home!".txt)
    }
}
this register botJoinedChannelReplier { join ->
    replyPublicly {
        +section("${join.channel.mention} is the best channel ever!".md)
    }
}