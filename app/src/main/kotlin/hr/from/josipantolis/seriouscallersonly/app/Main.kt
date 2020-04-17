@file:JvmName("Main")

package hr.from.josipantolis.seriouscallersonly.app

import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.servlet.SlackAppServlet
import hr.from.josipantolis.seriouscallersonly.api.Bot
import hr.from.josipantolis.seriouscallersonly.runtime.script.Loader
import hr.from.josipantolis.seriouscallersonly.runtime.slack.slackApp
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.context.support.beans
import org.springframework.core.env.get
import java.io.File
import java.time.Clock
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
    bean { Loader() }
    bean({ loader: Loader ->
        val scriptsDir = env["sco.scripts.dir"]!!
        loader.load(File(scriptsDir))
    })
    bean { Clock.systemUTC() }
    bean {
        AppConfig.builder()
            .singleTeamBotToken(env["sco.slack.bot.token"]!!)
            .signingSecret(env["sco.slack.signing.secret"]!!)
            .build()
    }
    bean({ bot: Bot, config: AppConfig, clock: Clock ->
        slackApp(bot = bot, config = config, clock = clock)
    })
    bean({ bot: Bot -> BotActuatorEndpoint(bot) })
}
