package net.polix.system.command.console

import com.google.gson.JsonParser
import net.polix.system.LOGGER
import net.polix.system.dialog.ConsoleCommand
import net.polix.system.dialog.dialog.CommandDialog
import net.polix.system.utility.PastebinUtil
import java.io.IOException

class PastebinCommand : ConsoleCommand(
    "paste", "паста"
) {

    override fun CommandDialog.handleInput() {
        if (args.size < 2) {
            response = "Введите текст!"
            return
        }

        try {
            val url = PastebinUtil.getUrl(JsonParser(), args.drop(1).joinToString(" "))
            response = "Паста создана [${url}]"
        } catch (e: IOException) {
            LOGGER.error(e.stackTraceToString())
        }
    }
}