@file:JvmName("Main")

package org.github.jantolis.seriouscallersonly.app

import org.github.jantolis.seriouscallersonly.bot.Bot
import org.github.jantolis.seriouscallersonly.dsl.Protocol
import org.github.jantolis.seriouscallersonly.dsl.protocol
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.router

@SpringBootApplication
class App

fun main(args: Array<String>) {
    runApplication<App>(*args) {
        addInitializers(beans())
    }
}

fun beans() = org.springframework.context.support.beans {
    bean { protocol { greeting = "Hello world!" } }
    bean<Bot, Protocol>({ Bot(listOf(it)) })
    bean({ bot: Bot -> Router(bot).routes() })
}

class Router(private val bot: Bot) {
    fun routes() = router {
        GET("/greeting") { ServerResponse.ok().body(bot.greet())}
    }
}