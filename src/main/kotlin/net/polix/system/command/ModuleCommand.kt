package net.polix.system.command

import com.google.gson.JsonObject
import com.vk.api.sdk.objects.messages.Keyboard
import com.vk.api.sdk.objects.messages.KeyboardButton
import com.vk.api.sdk.objects.messages.KeyboardButtonColor
import net.polix.system.dialog.UserCommand
import net.polix.system.dialog.dialog.UserCommandDialog
import net.polix.system.integration.route.vkontakte.VkBot
import net.polix.system.module.AbstractCoordinatorModule
import net.polix.system.module.CoordinatorModule
import net.polix.system.utility.ForwardMessage
import net.polix.system.utility.createbutton
import net.polix.system.utility.getTimeLeft
import java.io.File
import kotlin.random.Random

class ModuleCommand : UserCommand(
    "module", "модули", description = "COMMAND_MODULE_DESCRIPTION", permission = "admin.access"
) {
    override fun UserCommandDialog.handleInput() {
        if (args.size == 1) {
            responseReply =
                "❗Правильное использование команды:\n— /$command load <модуль> » загружает модуль\n— /$command unload <модуль> » отгружает модуль\n— /$command list » возвращает список модулей\n— /$command enable <модуль> » включает модуль\n— /$command disable <модуль> » отключает модуль"
            return
        }

        when (args[1].lowercase()) {
            "enable" -> {
                if (!user.has("command.module.enable")) {
                    responseReply = localizationService.findMessagePlaceholders(
                        user.lang,
                        "NO_PERMISSION",
                        arrayOf("%permission%", "command.module.enable")
                    )
                    return
                }
                if (args.size < 2) {
                    responseReply = "Вы не указали модуль"
                    return
                }

                val moduleName = args[2]

                val module = moduleService.getModule(moduleName)!!
                if (module.isEnable()) {
                    (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                        .randomId(Random.nextInt())
                        .message("⚠\uFE0FМодуль итак включен.")
                        .peerId(message!!.peerId.toInt())
                        .execute()
                    return
                }

                module.enable()
                (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                    .randomId(Random.nextInt())
                    .message("✅Включил ${module.getInfo().name} модуль!")
                    .peerId(message!!.peerId.toInt())
                    .execute()
            }

            "disable" -> {
                if (!user.has("command.module.disable")) {
                    responseReply = localizationService.findMessagePlaceholders(
                        user.lang,
                        "NO_PERMISSION",
                        arrayOf("%permission%", "command.module.disable")
                    )
                    return
                }
                if (args.size < 2) {
                    responseReply = "Вы не указали модуль"
                    return
                }

                val moduleName = args[2]

                val module = moduleService.getModule(moduleName)!!
                if (!module.isEnable()) {
                    (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                        .randomId(Random.nextInt())
                        .message("⚠\uFE0FМодуль итак выключен.")
                        .peerId(message!!.peerId.toInt())
                        .execute()
                    return
                }

                module.disable()
                (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                    .randomId(Random.nextInt())
                    .message("✅Выключил ${module.getInfo().name} модуль!")
                    .peerId(message!!.peerId.toInt())
                    .execute()
            }

            "list" -> {

                val builder = StringBuilder()

                val modules = moduleService.getModules()
                if (modules.isEmpty()) {
                    builder.append("<модулей нет>")
                } else {
                    for (module in moduleService.getModules()) {
                        builder.append("\n\n")
                        val status = if (module.isEnable()) "✅" else "\uD83D\uDEAB"
                        builder.append(
                            "$status${module.getInfo().name}, ${module.getInfo().version} - ${
                                module.getInfo().authors.joinToString(
                                    ", "
                                )
                            }\n"
                        )
                        builder.append(
                            "- ${
                                getTimeLeft(
                                    (System.currentTimeMillis() - module.getEnableMillis()) / 1000L,
                                    localizationService,
                                    user
                                )
                            }\n\n"
                        )
                    }
                }

                val countModules =
                    modules.takeIf { it.isNotEmpty() }?.let { "\uD83D\uDEE0\uFE0FВсего модулей: ${it.size}" } ?: ""

                val keyboard: Keyboard = if (modules.size > 0) {
                    Keyboard(command!!, moduleService.getModules().toList())
                } else {
                    Keyboard().setButtons(listOf()).setInline(true)
                }

                (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                    .forward(ForwardMessage(message))
                    .randomId(Random.nextInt())
                    .keyboard(keyboard)
                    .message("\uD83D\uDCDDСписок всех модулей: $builder\n\n$countModules")
                    .peerId(message!!.peerId.toInt())
                    .execute()
            }

            "reload" -> {
                if (!user.has("command.module.reload")) {
                    responseReply = localizationService.findMessagePlaceholders(
                        user.lang,
                        "NO_PERMISSION",
                        arrayOf("%permission%", "command.module.reload")
                    )
                    return
                }
                if (args.size < 2) {
                    responseReply = "Вы не указали модуль"
                    return
                }

                val moduleName = args.getOrNull(2)
                val module = moduleService.getModule(moduleName.toString())

                if (module == null) {
                    responseReply = "модуль не найден."
                    return
                }

                module.unload()
                moduleService.getModules().remove(module)

                try {
                    moduleService.initializeModule(File("modules/${moduleName}-plain.jar"))
                } catch (e: Exception) {
                    (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                        .message("Произошла ошибка: ${e.localizedMessage}, модуль не удалось инициализировать.")
                        .peerId(message!!.peerId.toInt())
                        .randomId(Random.nextInt())
                        .execute()
                    return
                }

                (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                    .message("✅Успешно перезагрузил модуль!")
                    .peerId(message!!.peerId.toInt())
                    .randomId(Random.nextInt())
                    .forward(ForwardMessage(message!!))
                    .execute()
            }

            "info" -> {
                if (!user.has("command.module.info")) {
                    responseReply = localizationService.findMessagePlaceholders(
                        user.lang,
                        "NO_PERMISSION",
                        arrayOf("%permission%", "command.module.info")
                    )
                    return
                }

                if (args.size < 3) {
                    responseReply = "Вы не указали модуль"
                    return
                }

                val module = args[2]
                val getModule = moduleService.getModule(module)

                if (getModule == null) {
                    responseReply = "Модуль не найден"
                    return
                }

                val aliasesAndCommands = getModule.getCommands()?.joinToString(" ") {
                    "\n» ${it.commandNames.first()} (${it.commandNames.drop(1).joinToString()})"
                } ?: "нету"

                val builder = StringBuilder(aliasesAndCommands)
                val authors = if (getModule.getInfo().authors.joinToString(", ")
                        .isEmpty()
                ) "нету" else getModule.getInfo().authors.joinToString(", ")
                (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                    .randomId(Random.nextInt())
                    .forward(ForwardMessage(message!!))
                    .message(
                        "❓Информация о модуле:\n" +
                                "— Модуль: ${getModule.getInfo().name}\n" +
                                "— Версия: ${getModule.getInfo().version}\n" +
                                "— Авторы: ${authors}\n" +
                                "— Задач: ${getModule.getCountTasks()}\n" +
                                "\n" +
                                "— Зарегистрировано команд: ${getModule.getCommands()?.size ?: 0}\n" +
                                "— Команды: $builder\n"
                    )
                    .peerId(message!!.peerId.toInt())
                    .execute()

            }

            "load" -> {
                if (!user.has("command.module.load")) {
                    responseReply = localizationService.findMessagePlaceholders(
                        user.lang,
                        "NO_PERMISSION",
                        arrayOf("%permission%", "command.module.load")
                    )
                    return
                }
                if (args.size < 2) {
                    responseReply = "Вы не указали модуль"
                    return
                }

                val moduleName = args[2]
                val moduleFile = File("modules/${moduleName}-plain.jar")

                if (args.getOrNull(2)?.lowercase() == "*") {
                    val modules = moduleService.loadModules(File("modules"))

                    if (modules.isEmpty()) {
                        (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                            .randomId(Random.nextInt())
                            .peerId(message!!.peerId.toInt())
                            .forward(ForwardMessage(message!!))
                            .message("Не удалось загрузить модули, нету не загруженных модулей.")
                            .execute()
                        return
                    }

                    (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                        .randomId(Random.nextInt())
                        .peerId(message!!.peerId.toInt())
                        .forward(ForwardMessage(message!!))
                        .keyboard(createKeyboard(command!!, modules))
                        .message("Загрузил ${modules.size} моду(ля/лей)")
                        .execute()

                    return
                }

                if (!moduleFile.exists()) {
                    (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                        .message("Модуль $moduleName не найден.")
                        .peerId(message!!.peerId.toInt())
                        .randomId(Random.nextInt())
                        .forward(ForwardMessage(message!!))
                        .execute()
                    return
                }


                val moduleInfo = moduleService.initializeModule(moduleFile)

                if (moduleInfo != null) {
                    val aliasesAndCommands =
                        moduleService.getModule(moduleInfo.moduleInfo.name)?.getCommands()?.joinToString(" ") {
                            "\n» ${it.commandNames.first()} (${it.commandNames.drop(1).joinToString()})"
                        } ?: "нету"
                    val builder = StringBuilder(aliasesAndCommands)
                    (bot as VkBot).vkApiClient.messages().send((bot as VkBot).groupActor)
                        .randomId(Random.nextInt())
                        .forward(ForwardMessage(message!!))
                        .message(
                            "✅Модуль успешно загружен!\n" +
                                    "— Модуль: ${moduleInfo.getInfo().name}\n" +
                                    "— Версия: ${moduleInfo.getInfo().version}\n" +
                                    "— Авторы: ${moduleInfo.getInfo().authors.joinToString(", ")}\n" +
                                    "— Задач: ${moduleInfo.getCountTasks()}\n\n" +
                                    "— Зарегистрировано команд: ${moduleInfo.getCommands().size}\n" +
                                    "— Команды: $builder\n"
                        )
                        .peerId(message!!.peerId.toInt())
                        .execute()
                }
            }

            "unload" -> {
                if (!user.has("command.module.unload")) {
                    responseReply = localizationService.findMessagePlaceholders(
                        user.lang,
                        "NO_PERMISSION",
                        arrayOf("%permission%", "command.module.unload")
                    )
                    return
                }
                if (args.size < 2) {
                    responseReply = "Вы не указали модуль"
                    return
                }

                val getModule = moduleService.getModule(args[2])
                val moduleIterator = moduleService.getModules()

                if (moduleIterator.isEmpty()) {
                    responseReply = "Нечего выгружать, модули не загружены"
                    return
                }

                if (args.getOrNull(2) == "*") {
                    val iterator = moduleService.getModules().iterator()
                    while (iterator.hasNext()) {
                        val module = iterator.next()
                        iterator.remove()
                        module.unload()
                    }

                    responseReply = "Успешно отгрузил все модули!"

                    return
                }

                if (getModule == null) {
                    responseReply = "Модуль не найден!"
                    return
                }

                getModule.unload()
                moduleIterator.remove(getModule)

                responseReply = "Успешно отгрузил ${getModule.getInfo().name} модуль!"
            }

            else -> responseReply = "Вы ввели не верную под команду."
        }
    }
    private fun Keyboard(command: String, modules: List<CoordinatorModule>): Keyboard {
        val chunks = modules.chunked(3)

        val keyboardButtons = mutableListOf<List<KeyboardButton>>()

        chunks.forEach { chunk ->
            val chunkButtons = chunk.map { module ->
                val payload = JsonObject().apply {
                    addProperty("command", "$command info ${module.getInfo().name}")
                }

                createbutton(module.getInfo().name, payload, KeyboardButtonColor.DEFAULT)
            }
            keyboardButtons.add(chunkButtons)
        }

        return Keyboard().apply {
            inline = true
            buttons = keyboardButtons
        }
    }

    private fun createKeyboard(command: String, modules: List<AbstractCoordinatorModule>): Keyboard {
        val chunks = modules.chunked(3)

        val keyboardButtons = mutableListOf<List<KeyboardButton>>()

        chunks.forEach { chunk ->
            val chunkButtons = chunk.map { module ->
                val payload = JsonObject().apply {
                    addProperty("command", "$command info ${module.moduleInfo.name}")
                }

                createbutton(module.moduleInfo.name, payload, KeyboardButtonColor.DEFAULT)
            }
            keyboardButtons.add(chunkButtons)
        }

        return Keyboard().apply {
            inline = true
            buttons = keyboardButtons
        }
    }
}