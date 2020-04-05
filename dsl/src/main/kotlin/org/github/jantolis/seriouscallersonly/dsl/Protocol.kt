package org.github.jantolis.seriouscallersonly.dsl

@DslMarker
annotation class ProtocolMarker

@ProtocolMarker
class Protocol {
    var userJoinCallback: ReplyToUserJoin = {}
    var newMessageCallback: ReplyToMessage = {}

    fun onUserJoin(reply: ReplyToUserJoin) {
        userJoinCallback = reply
    }

    fun onNewMessage(reply: ReplyToMessage) {
        newMessageCallback = reply
    }
}

fun protocol(init: Protocol.() -> Unit): Protocol {
    val proto = Protocol()
    proto.init()
    return proto
}