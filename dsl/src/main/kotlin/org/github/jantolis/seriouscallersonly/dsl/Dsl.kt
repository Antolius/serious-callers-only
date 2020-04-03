package org.github.jantolis.seriouscallersonly.dsl

class Protocol {
    var greeting: String? = null
}

fun protocol(init: Protocol.() -> Unit): Protocol {
    val proto = Protocol()
    proto.init()
    return proto
}