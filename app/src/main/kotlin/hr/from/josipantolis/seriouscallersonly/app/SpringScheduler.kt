package hr.from.josipantolis.seriouscallersonly.app

import hr.from.josipantolis.seriouscallersonly.runtime.slack.Scheduler
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.support.CronTrigger
import java.util.*

class SpringScheduler(private val taskScheduler: TaskScheduler) : Scheduler {
    override fun schedule(cron: String, job: Runnable) {
        taskScheduler.schedule(job, CronTrigger(cron, TimeZone.getTimeZone("UTC")))
    }
}