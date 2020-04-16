@file:JvmName("Main")

package hr.from.josipantolis.seriouscallersonly.app

import com.slack.api.bolt.App
import com.slack.api.bolt.servlet.SlackAppServlet
import hr.from.josipantolis.seriouscallersonly.runtime.script.Loader
import hr.from.josipantolis.seriouscallersonly.runtime.slack.slackApp
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.context.support.beans
import java.io.File
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
        val scriptsPath = this::class.java.classLoader.getResource("scripts")!!.path
        slackApp(
            bot = Loader().load(File(scriptsPath))
        )
    }
}
