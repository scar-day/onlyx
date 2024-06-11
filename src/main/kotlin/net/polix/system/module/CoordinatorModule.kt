package net.polix.system.module

import net.polix.system.dialog.BaseDialog
import net.polix.system.event.listener.EventListener
import net.polix.system.module.mapping.ModuleInfo
import w.config.FileConfig

interface CoordinatorModule {

    fun getInfo(): ModuleInfo

    fun enable()

    fun disable()

    fun getEnableMillis(): Long

    fun reload() {
        disable()
        enable()
    }

    fun load()

    fun unload()

    fun isEnable(): Boolean

    fun getCommands(): Set<BaseDialog<*>>?

    fun getListeners(): Set<EventListener>

    fun registerCommands(vararg commands: BaseDialog<*>)

    fun registerCommand(command: BaseDialog<*>)

    //fun registerListeners(`package`: String)

    fun registerListener(listener: EventListener)


    fun getConfig(): FileConfig?

    fun getCountTasks(): Int

}