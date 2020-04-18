package hr.from.josipantolis.seriouscallersonly.runtime.slack

interface Scheduler {
    fun schedule(cron: String, job: Runnable)
}