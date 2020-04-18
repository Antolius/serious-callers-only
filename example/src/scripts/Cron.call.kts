//this register channelProtocol("C012340RJ91".channel) {
channelProtocol("C012340RJ91".channel) {
    onTimer("* 0/5 * * * *") {
        replyPublicly {
            +section("Hello ${User.specialMention.here}!".md)
        }
    }
}