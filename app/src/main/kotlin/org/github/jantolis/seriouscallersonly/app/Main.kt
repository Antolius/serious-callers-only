@file:JvmName("Main")

package org.github.jantolis.seriouscallersonly.app

import com.slack.api.bolt.App
import com.slack.api.bolt.servlet.SlackAppServlet
import org.github.jantolis.seriouscallersonly.bot.slackApp
import org.github.jantolis.seriouscallersonly.dsl.Bot
import org.github.jantolis.seriouscallersonly.dsl.Channel
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.context.support.beans
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

fun beans() = beans {
    bean {
        slackApp(bot = Bot(channelProtocols = mapOf(
                Channel("C0114PUSDU2") to gameProtocol()
        )))
    }
}
