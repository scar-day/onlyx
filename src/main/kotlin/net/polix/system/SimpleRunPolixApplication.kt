package net.polix.system

import jakarta.annotation.PostConstruct
import net.polix.system.command.*
import net.polix.system.command.console.PastebinCommand
import net.polix.system.localization.LocalizationService
import net.polix.system.dialog.DialogCommandService
import net.polix.system.event.EventService
import net.polix.system.integration.route.vkontakte.VkRoute
import net.polix.system.integration.route.vkontakte.provider.impl.GroupProvider
import net.polix.system.module.ModuleService
import net.polix.system.scheduler.SchedulerService
import net.polix.system.user.UserService
import net.polix.system.user.status.StatusService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

val LOGGER: Logger = LoggerFactory.getLogger(PolixApplication::class.java)

@Service
class SimplePolixApplication {

    @Autowired
    lateinit var dialogCommandService: DialogCommandService

    @Autowired
    lateinit var moduleService: ModuleService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var statusService: StatusService

    @Autowired
    lateinit var localizationService: LocalizationService

    @Autowired
    lateinit var schedulerService: SchedulerService

    @Autowired
    lateinit var eventService: EventService

    lateinit var vk: GroupProvider
    @PostConstruct
    fun initialize() {
        moduleService.initialize()

        localizationService.parseFromRepository(
            "only-lab",
            "localization"
        )

        LOGGER.info("[Core] <-> Запускаю инициализацию команд")

        initializeCommands()

        LOGGER.info("[Core] <-> Запускаю инициализацию статусов")

        createStatus()

        vk = GroupProvider(
            1,
            "???",
            eventService
        )

        VkRoute(vk).handleEvents()

    }

    fun createStatus() {
        statusService.createStatus(
            "user",
            1,
            "Пользователь"
        )

        statusService.createStatus(
            "moder",
            2,
            "Модератор",
            mutableListOf("moder.access")
        )

        statusService.createStatus("admin",
            999,
            "Администратор",
            mutableListOf("core.access", "admin.access"))
    }

    fun initializeCommands() {
        dialogCommandService.registerCommand(UserCommand())
        dialogCommandService.registerCommand(BotCommand())
        dialogCommandService.registerCommand(AdminCommand())
        dialogCommandService.registerCommand(LangCommand())
        dialogCommandService.registerCommand(CoreCommand())
        dialogCommandService.registerCommand(IdCommand())
        dialogCommandService.registerCommand(ModuleCommand())
        dialogCommandService.registerCommand(HelpCommand())
        dialogCommandService.registerCommand(AliassesCommand())
        dialogCommandService.registerCommand(net.polix.system.command.console.HelpCommand())
        dialogCommandService.registerCommand(PastebinCommand())
    }


}