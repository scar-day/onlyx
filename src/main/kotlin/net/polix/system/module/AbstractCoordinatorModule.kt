package net.polix.system.module

import net.polix.system.LOGGER
import net.polix.system.dialog.BaseDialog
import net.polix.system.dialog.DialogCommandService
import net.polix.system.event.listener.EventListener
import net.polix.system.module.mapping.ModuleInfo
import net.polix.system.scheduler.SchedulerService
import w.config.FileConfig
import w.config.SimpleFileConfig
import java.io.File
import java.util.jar.JarFile


abstract class AbstractCoordinatorModule : CoordinatorModule {

    lateinit var commandService: DialogCommandService
    lateinit var schedulerService: SchedulerService

    lateinit var moduleInfo: ModuleInfo

    lateinit var file: File
    lateinit var jarFile: JarFile

    private var config: FileConfig? = null
    private var enableMillis = 0L
    private var enable = false
    private val commands = mutableSetOf<BaseDialog<*>>()
    private val listeners = mutableSetOf<EventListener>()

    open fun onEnable() {}

    open fun onDisable() {}

    override fun getInfo() = moduleInfo

    override fun enable() {
        if (enable) {
            return
        }

        onEnable()

        commands.forEach {
            commandService.registerCommand(it)
        }

        LOGGER.info("[ModuleService] <-> Module ${moduleInfo.name} is enabled v${moduleInfo.version} by authors ${moduleInfo.authors.joinToString(", ")}")

        enableMillis = System.currentTimeMillis()
        enable = true
    }

    override fun getCountTasks(): Int {
        return schedulerService.schedulerMap.size
    }

    override fun disable() {
        if (!enable) {
            return
        }

        onDisable()

        commands.forEach {
            commandService.unregisterCommand(it)
        }

        commands.clear()
        jarFile.close()

        LOGGER.info("[ModuleService] <-> Module ${moduleInfo.name} is disabled v${moduleInfo.version} by authors ${moduleInfo.authors.joinToString(", ")}")

        enableMillis = System.currentTimeMillis()
        enable = false
    }

    override fun load() {
        if (enable) {
            return
        }

        jarFile = JarFile(file)

        enable()
    }

    override fun unload() {
        if (!enable) {
            return
        }

        jarFile.close()

        disable()
    }

    override fun isEnable() = enable


    override fun getListeners() = listeners

    override fun getEnableMillis(): Long = enableMillis

    override fun registerCommand(command: BaseDialog<*>) {
        this.commands.add(command)
    }

    override fun registerCommands(vararg commands: BaseDialog<*>) {
        this.commands.addAll(commands)
    }

    override fun registerListener(listener: EventListener) {
        listeners.add(listener)
    }

    override fun getConfig() = config

    fun loadConfig(path: String) {
        if (!File("modules/${moduleInfo.name}").exists()) {
            File("modules/${moduleInfo.name}").mkdir()
        }

        config = SimpleFileConfig.create(File("modules/${moduleInfo.name}/$path.yml")).also { it.saveDefaults("/$path.yml") }
    }

    fun saveConfig() {
        config?.save()
    }

    override fun getCommands(): Set<BaseDialog<*>> {
        return commands
    }
}