@file:JvmName("Main")

package org.github.jantolis.seriouscallersonly.app

import org.github.jantolis.seriouscallersonly.bot.Bot
import org.github.jantolis.seriouscallersonly.dsl.protocol

fun main() {
    val bot = Bot(protocols = listOf(protocol {
        greeting = "Hello world!"
    }))
    bot.greet()
}