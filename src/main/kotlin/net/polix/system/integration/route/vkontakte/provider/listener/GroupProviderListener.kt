package net.polix.system.integration.route.vkontakte.provider.listener

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import jakarta.annotation.PostConstruct
import net.polix.system.LOGGER
import net.polix.system.SimplePolixApplication
import net.polix.system.localization.LocalizationService
import net.polix.system.dialog.DialogCommandService
import net.polix.system.dialog.UserCommand
import net.polix.system.dialog.dialog.UserCommandDialog
import net.polix.system.dialog.dialog.impl.UserCommandDialogImpl
import net.polix.system.event.EventService
import net.polix.system.event.listener.EventListener
import net.polix.system.integration.IntegrationType
import net.polix.system.integration.route.vkontakte.VkBot
import net.polix.system.integration.route.vkontakte.event.group.VkGroupNewMessageEvent
import net.polix.system.module.ModuleService
import net.polix.system.scheduler.SchedulerService
import net.polix.system.user.UserService
import net.polix.system.user.status.StatusService
import net.polix.system.utility.*
import org.springframework.stereotype.Component
import w.eventbus.Subscribe
import java.util.regex.Pattern
import kotlin.random.Random

@Component
class GroupProviderListener(
    val commandService: DialogCommandService,
    val eventService: EventService,
    val localizationService: LocalizationService,
    val userService: UserService,
    val statusService: StatusService,
    val moduleService: ModuleService,
    val runpolix: SimplePolixApplication,
    val schedulerService: SchedulerService,
) : EventListener {


    @PostConstruct
    fun init() {
        eventService.eventBus.register(this)
    }


    @Subscribe
    fun handleMessage(event: VkGroupNewMessageEvent) {
        val message = event.message

        if (message.fromId < 0) return
        val objectMapper = jacksonObjectMapper()
        val commandPrefixes = listOf("/", "!")

        val payload = message.payload
        val commandpayload = Gson().fromJson(payload, JsonObject::class.java)?.get("command")?.asString?.trim()
        val args = commandpayload?.split(" ")?.toTypedArray() ?: message.text.split(" ").toTypedArray()

        val user = userService.createUser(message.fromId, IntegrationType.VK)

        var commandName = args[0]
        val isChat = message.peerId > 2000000000

        val lastTalker = user.dialogs[message.fromId.toInt()]
            ?.let { objectMapper.readValue(it, UserCommand::class.java) }

        val commandDialog: UserCommandDialog = UserCommandDialogImpl(
            content = args.joinToString(" "),
            command = null,
            message = message,
            isChat = isChat,
            replyMessage = message.replyMessage,
            activeDialog = lastTalker,
            moduleService = moduleService,
            userService = userService,
            statusService = statusService,
            localizationService = localizationService,
            user = user,
            commandService = commandService,
            bot = VkBot(event.provider.getActor()),
            schedulerService = schedulerService,
        )

        try {
            if (lastTalker != null) {
                val result = lastTalker.handle(commandDialog)

                user.dialogs[message.fromId.toInt()] = objectMapper.writeValueAsString(result)
                userService.repository.save(user)

                return
            }

            val messageText = message.text
            val pattern = Pattern.compile("\\[club(\\d+)")
            val matcher = pattern.matcher(messageText)

            if (matcher.lookingAt()) {
                val clubIdFromMessage = matcher.group(1)
                val actorIdWithoutDash = event.provider.getActor().id.toString().replace("-", "")

                if (clubIdFromMessage == actorIdWithoutDash && message.payload!!.isEmpty()) {
                    val stickerIds = arrayOf(79407, 69, 65119, 79107, 78617, 84228, 79398)
                    event.provider.getMethods().messages().send(event.provider.getActor())
                        .stickerId(stickerIds.random())
                        .forward(ForwardMessage(message))
                        .randomId(Random.nextInt())
                        .peerId(message.peerId.toInt())
                        .execute()
                }
            }

            if (commandPrefixes.any { commandName.startsWith(it) }) {
                commandName = commandName.substring(1)
            } else if (message.payload != null) {
                val payloadObject = Gson().fromJson(message.payload, JsonObject::class.java)

                val commandPayload = payloadObject?.getAsJsonPrimitive("command")?.asString ?: ""
                commandName = commandPayload.split(" ").firstOrNull { command ->
                    !commandPrefixes.any { prefix -> command.startsWith(prefix) }
                } ?: ""
            } else if (isChat) {
                return
            }

            if (commandName.isEmpty() || commandName.isBlank()) {
                return
            }

            val command = commandService.getCommand(commandName.lowercase()) ?: return

            if (command.permission != null) {
                if (!user.has(command.permission.toString())) {
                    event.provider.getMethods().messages().send(event.provider.getActor())
                        .message(localizationService.findMessagePlaceholders(user.lang, "NO_PERMISSION", arrayOf("%permission%", command.permission)))
                        .peerId(message.peerId.toInt())
                        .forward(ForwardMessage(message))
                        .randomId(Random.nextInt())
                        .execute()

                    return
                }
            }

            if (isCommandInCooldownList(message.fromId.toInt(), commandName)) {
                event.provider.getMethods().messages().send(event.provider.getActor())
                    .message(localizationService.findMessagePlaceholders(user.lang, "COOLDOWN", arrayOf(
                        "%cooldown%", getTimeLeft(getRemainingCooldown(message.fromId.toInt(), commandName), localizationService, user)
                    )))
                    .peerId(message.peerId.toInt())
                    .forward(ForwardMessage(message))
                    .randomId(Random.nextInt())
                    .execute()
                return
            }

            if (command.cooldown > 0) {
                addCommands(message.fromId.toInt(), command.commandNames, command.cooldown)
            }

            if (command.isConsole) {
                return
            }

            LOGGER.info("[EventService -> VK] <-> Пользователь ${message.fromId} использовал команду: $commandName ${args.copyOfRange(1, args.size).joinToString(" ")}")

            commandDialog.command = commandName
            val result = (command as UserCommand).handle(commandDialog)

            user.dialogs[message.fromId.toInt()] = objectMapper.writeValueAsString(result)
            userService.repository.save(user)
        } catch (ex: Exception) {
            event.provider.getMethods().messages().send(event.provider.getActor()).forward(ForwardMessage(message)).message(localizationService.findMessage(user.lang, "ERROR")).peerId(message.peerId.toInt()).randomId(Random.nextInt()).execute()
            LOGGER.error("[CommandService] <-> An error occurred while processing the command:")
            ex.printStackTrace()
        }
    }


}