package net.polix.system.scheduler

import java.util.concurrent.TimeUnit


abstract class SchedulerCommon : Runnable {
    private val identifier: String = ""
    private val schedulerMananger = SchedulerService()

    /**
     * Отмена и закрытие потока
     */
    fun cancel() {
        schedulerMananger.cancelScheduler(identifier)
    }

    /**
     * Запустить асинхронный поток
     */
    fun runAsync() {
        schedulerMananger.runAsync(this)
    }

    /**
     * Запустить поток через определенное
     * количество времени
     *
     * @param delay - время
     * @param timeUnit - единица времени
     */
    fun runLater(delay: Long, timeUnit: TimeUnit) {
        schedulerMananger.runLater(identifier, this, delay, timeUnit)
    }

    /**
     * Запустить цикличный поток через
     * определенное количество времени
     *
     * @param delay - время
     * @param period - период цикличного воспроизведения
     * @param timeUnit - единица времени
     */
    fun runTimer(delay: Long, period: Long, timeUnit: TimeUnit) {
        schedulerMananger.runTimer(identifier, this, delay, period, timeUnit)
    }
}
