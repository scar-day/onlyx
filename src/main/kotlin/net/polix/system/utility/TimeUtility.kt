package net.polix.system.utility

import net.polix.system.localization.LocalizationService
import net.polix.system.user.User
import kotlin.math.abs

fun getTimeLeft(number: Long, localizationService: LocalizationService, user: User): String {

    val oneday = localizationService.findMessage(user.lang, "TIME_DAY_1")!!.split("\n")
    val onehour = localizationService.findMessage(user.lang, "TIME_HOURS_1")!!.split("\n")
    
    val oneminute = localizationService.findMessage(user.lang, "TIME_MINUTES_1")!!.split("\n")
    val twominute = localizationService.findMessage(user.lang, "TIME_MINUTES_2")!!.split("\n")

    val onesecond = localizationService.findMessage(user.lang, "TIME_SECOND_1")!!.split("\n")
    val twosecond = localizationService.findMessage(user.lang, "TIME_SECOND_2")!!.split("\n")

    when (number) {
        0L -> {
            return "0 ${onesecond[0]}"
        }
        1L -> {
            return "1 ${onesecond[0] + onesecond[1]}"
        }
        else -> {
            var sec = number
            var m = sec / 60L
            sec %= 60L
            var h = m / 60L
            m %= 60L
            val d = h / 24L
            h %= 24L
            var time = ""

            if (d > 0L) {
                time = time + d + " " + formatTime(d, oneday[0] + oneday[1], oneday[0] + oneday[2], oneday[0] + oneday[3])
                if (h > 0L || m > 0L || sec > 0L) {
                    time = "$time "
                }
            }

            if (h > 0L) {
                time = time + h + " " + formatTime(h, onehour[0],  onehour[0] + onehour[2], onehour[0] + onehour[3])
                if (m > 0L || sec > 0L) {
                    time = "$time "
                }
            }

            if (m > 0L) {
                time = time + m + " " + formatTime(m, oneminute[0] + twominute[1], oneminute[0] + oneminute[2], oneminute[0])
                if (sec > 0L) {
                    time = "$time "
                }
            }

            if (sec > 0L) {
                time =
                    time + sec + " " + formatTime(sec, onesecond[0] + twosecond[1], onesecond[0] + onesecond[2], onesecond[0])
            }

            return time
        }
    }
}

fun formatTime(number: Long, caseOne: String, caseTwo: String, caseFive: String): String {
    val time: Long = abs(number)

    val str = if (time % 10 == 1L && time % 100 != 11L) {
        caseOne
    } else if (time % 10 in 2..4 && (time % 100 < 10 || time % 100 >= 20)) {
        caseTwo
    } else {
        caseFive
    }

    return str
}

fun getTimeLeft(seconds: Long): String {
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    val builder = StringBuilder()

    if (days > 0) builder.append("$days дн. ")
    if (hours > 0) builder.append("$hours ч. ")
    if (minutes > 0) builder.append("$minutes мин. ")

    builder.append("$remainingSeconds сек.")

    return builder.toString()
}

