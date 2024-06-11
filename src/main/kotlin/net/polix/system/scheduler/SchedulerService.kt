package net.polix.system.scheduler

import org.springframework.stereotype.Service
import java.util.concurrent.*

@Service
class SchedulerService {

    val schedulerMap: MutableMap<String, ScheduledFuture<*>> = ConcurrentHashMap()
    val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(10)

    /**
     * Run a task asynchronously.
     *
     * @param command The task to run.
     */
    fun runAsync(command: Runnable) {
        scheduledExecutor.submit(command)
    }

    /**
     * Cancel and remove a scheduled task by its ID.
     *
     * @param schedulerId The ID of the scheduled task.
     */
    fun cancelScheduler(schedulerId: String) {
        schedulerMap[schedulerId.lowercase()]?.cancel(true)
        schedulerMap.remove(schedulerId.lowercase())
    }

    /**
     * Schedule a task to be executed after a delay.
     *
     * @param schedulerId The ID of the scheduler.
     * @param command The task to be executed.
     * @param delay The delay before execution.
     * @param timeUnit The time unit for the delay.
     */
    fun runLater(schedulerId: String, command: Runnable, delay: Long, timeUnit: TimeUnit) {
        val scheduledFuture = scheduledExecutor.schedule(
            { command.run(); schedulerMap.remove(schedulerId.lowercase()) },
            delay,
            timeUnit
        )
        schedulerMap[schedulerId.lowercase()] = scheduledFuture
    }

    /**
     * Schedule a task to be executed periodically.
     *
     * @param schedulerId The ID of the scheduler.
     * @param command The task to be executed.
     * @param delay The delay before the first execution.
     * @param period The period between executions.
     * @param timeUnit The time unit for the delay and period.
     */
    fun runTimer(schedulerId: String?, command: Runnable, delay: Long, period: Long, timeUnit: TimeUnit) {
        val scheduledFuture = scheduledExecutor.scheduleAtFixedRate(
            command,
            delay,
            period,
            timeUnit
        )
        schedulerMap[schedulerId?.lowercase() ?: ""] = scheduledFuture
    }

    /**
     * Shutdown the scheduler service.
     */
    fun shutdown() {
        scheduledExecutor.shutdown()
    }

    /**
     * Await termination of the scheduler service.
     *
     * @param timeout The maximum time to wait for termination.
     * @param timeUnit The time unit of the timeout.
     */
    fun awaitTermination(timeout: Long, timeUnit: TimeUnit): Boolean {
        return scheduledExecutor.awaitTermination(timeout, timeUnit)
    }
}