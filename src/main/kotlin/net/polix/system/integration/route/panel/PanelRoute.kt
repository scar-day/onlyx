package net.polix.system.integration.route.panel

import net.polix.system.integration.IntegrationType
import net.polix.system.integration.route.Route

class PanelRoute : Route {
    override fun getType(): IntegrationType = IntegrationType.PANEL

    override fun handleEvents() {
    }
}