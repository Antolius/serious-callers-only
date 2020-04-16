@file:JvmName("Main")

package hr.from.josipantolis.seriouscallersonly.app

import com.slack.api.bolt.App
import com.slack.api.bolt.servlet.SlackAppServlet
import hr.from.josipantolis.seriouscallersonly.app.games.playCommandProtocol
import hr.from.josipantolis.seriouscallersonly.bot.slackApp
import hr.from.josipantolis.seriouscallersonly.dsl.Bot
import hr.from.josipantolis.seriouscallersonly.dsl.Command
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
        slackApp(bot = Bot(
                commandProtocols = mapOf(
                        Command("/play") to playCommandProtocol()
                )
        ))
    }
}
