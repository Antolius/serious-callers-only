@file:JvmName("Main")

package hr.from.josipantolis.seriouscallersonly.app

import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.servlet.SlackAppServlet
import hr.from.josipantolis.seriouscallersonly.api.Bot
import hr.from.josipantolis.seriouscallersonly.app.actuator.BotEndpoint
import hr.from.josipantolis.seriouscallersonly.app.actuator.ConversationsEndpoint
import hr.from.josipantolis.seriouscallersonly.runtime.repo.Repo
import hr.from.josipantolis.seriouscallersonly.runtime.script.Loader
import hr.from.josipantolis.seriouscallersonly.runtime.slack.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.context.support.beans
import org.springframework.core.env.AbstractEnvironment
import org.springframework.core.env.get
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.File
import java.time.Clock
import javax.servlet.annotation.WebServlet

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
class BootApp

@WebServlet("/slack/events")
class SlackController(app: App) : SlackAppServlet(app)

fun main(args: Array<String>) {
    runApplication<BootApp>(*args) {
        addInitializers(beans())
    }
}

fun beans() = beans {
    bean({ env: AbstractEnvironment -> ScriptProperties(env) })
    bean({ props: ScriptProperties -> Loader(env = props) })
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
    bean({ taskScheduler: TaskScheduler -> SpringScheduler(taskScheduler) })
    bean { conversationsRepo() }
    bean({ bot: Bot, scheduler: Scheduler, config: AppConfig, clock: Clock, conversations: Repo<ConversationKey, Conversation> ->
        slackApp(bot = bot, scheduler = scheduler, config = config, clock = clock, conversations = conversations)
    })
    bean({ bot: Bot -> BotEndpoint(bot) })
    bean({ conversations: Repo<ConversationKey, Conversation> -> ConversationsEndpoint(conversations) })
}
