package net.polix.system.module

import net.polix.system.LOGGER
import net.polix.system.dialog.DialogCommandService
import net.polix.system.module.mapping.ModuleInfo
import net.polix.system.scheduler.SchedulerService
import org.springframework.stereotype.Service
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile


@Service
class ModuleService(
    val dialogCommandService: DialogCommandService,
    val SchedulerService: SchedulerService
) {

    private val modules = mutableSetOf<CoordinatorModule>()

    fun getModules() = modules

    fun loadModules(modulesFolder: File): List<AbstractCoordinatorModule> {
        val moduleFileArray = modulesFolder.listFiles() ?: return emptyList()
        val initializedModules = mutableListOf<AbstractCoordinatorModule>()

        for (moduleFile in moduleFileArray) {
            val initializedModule = initializeModule(moduleFile)
            initializedModule?.let {
                initializedModules.add(it)
            }
        }

        return initializedModules
    }



    fun initialize() {
        LOGGER.info("[ModuleService] <-> Loading modules...")

        val files = File("modules").apply { mkdirs() }.listFiles()

        if (files == null || files.isEmpty()) {
            LOGGER.info("[ModuleService] <-> Modules not found, loading cancelled")

            return
        }

        files.filter { it.name.endsWith(".jar") }.forEach {
            val moduleJar = JarFile(it)
            val entries = moduleJar.entries()

            val classLoader = URLClassLoader(arrayOf(it.toURI().toURL()), javaClass.classLoader)

            while (entries.hasMoreElements()) {
                val jarEntry = entries.nextElement()

                if (jarEntry.isDirectory || !jarEntry.name.endsWith(".class")) {
                    continue
                }

                val className = jarEntry.name.substring(0, jarEntry.name.length - 6).replace("/", ".")

                if (className == "META-INF.versions.9.module-info") {
                    continue
                }

                try {
                    val clazz = Class.forName(className, true, classLoader)

                    if (!clazz.isAnnotationPresent(ModuleInfo::class.java)) {
                        continue
                    }

                    val moduleInfo = clazz.getAnnotation(ModuleInfo::class.java)
                    val module = clazz.newInstance() as AbstractCoordinatorModule

                    with(module) {
                        this.moduleInfo = moduleInfo
                        this.commandService = dialogCommandService
                        this.schedulerService = SchedulerService

                        file = it
                        jarFile = moduleJar

                        enable()
                    }

                    modules.add(module)
                } catch (ignored: ClassNotFoundException) {
                } catch (ignored: NoClassDefFoundError) {}
            }
        }
    }

    fun initializeModule(file: File): AbstractCoordinatorModule? {
        val moduleJar = JarFile(file)
        val entries = moduleJar.entries()
        val classLoader = URLClassLoader(arrayOf(file.toURI().toURL()), javaClass.classLoader)
        var initializedModule: AbstractCoordinatorModule? = null

        while (entries.hasMoreElements()) {
            val jarEntry = entries.nextElement()

            if (!jarEntry.isDirectory && jarEntry.name.endsWith(".class")) {
                val className = jarEntry.name.substring(0, jarEntry.name.length - 6).replace("/", ".")

                if (className == "META-INF.versions.9.module-info") {
                    continue
                }

                try {
                    val clazz = Class.forName(className, true, classLoader)

                    if (clazz.isAnnotationPresent(ModuleInfo::class.java)) {
                        val moduleInfo = clazz.getAnnotation(ModuleInfo::class.java)

                        if (!isModuleEnabled(moduleInfo.name)) {
                            val module = clazz.newInstance() as AbstractCoordinatorModule

                            with(module) {
                                this.moduleInfo = moduleInfo
                                this.commandService = dialogCommandService
                                this.schedulerService = SchedulerService

                                this.file = file
                                this.jarFile = moduleJar
                                enable()
                            }

                            modules.add(module)
                            initializedModule = module
                            break
                        }
                    }
                } catch (ignored: ClassNotFoundException) {
                } catch (ignored: NoClassDefFoundError) {}
            }
        }

        return initializedModule
    }

    private fun isModuleEnabled(moduleName: String): Boolean {
        return modules.any { it.getInfo().name == moduleName && it.isEnable() }
    }



    fun getModule(name: String) = modules.firstOrNull { it.getInfo().name.lowercase() == name.lowercase() }

}
