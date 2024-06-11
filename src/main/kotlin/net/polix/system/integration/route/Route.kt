package net.polix.system.integration.route

import net.polix.system.integration.IntegrationType

interface Route {

    fun getType(): IntegrationType

    fun handleEvents()

}