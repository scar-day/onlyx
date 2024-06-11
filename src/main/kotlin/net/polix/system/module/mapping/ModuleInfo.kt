package net.polix.system.module.mapping

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModuleInfo(
    val name: String,
    val version: String,
    vararg val authors: String
)
