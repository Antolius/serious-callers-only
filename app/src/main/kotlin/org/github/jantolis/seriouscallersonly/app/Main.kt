@file:JvmName("Main")

package org.github.jantolis.seriouscallersonly.app

import com.slack.api.bolt.App
import com.slack.api.bolt.servlet.SlackAppServlet
import org.github.jantolis.seriouscallersonly.bot.bot
import org.github.jantolis.seriouscallersonly.dsl.Protocol
import org.github.jantolis.seriouscallersonly.dsl.protocol
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import javax.servlet.annotation.WebServlet

@SpringBootApplication
@ServletComponentScan
class BootApp

@WebServlet("/slack/events")
class SlackController(app: App) : SlackAppServlet(app)

fun main(args: Array<String>) {
    runApplication<BootApp>(*args) {
        addInitializers(beans())
    }
}

fun beans() = org.springframework.context.support.beans {
    bean { bot(mapOf("C0114PUSDU2" to gameProtocol())) }
}
