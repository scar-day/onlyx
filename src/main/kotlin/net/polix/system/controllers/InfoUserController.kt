package net.polix.system.controllers

import net.polix.system.localization.LocalizationService
import net.polix.system.controllers.model.GroupInfo
import net.polix.system.controllers.model.StatusInfoResponse
import net.polix.system.controllers.model.UserInfoResponse
import net.polix.system.controllers.model.UserServersResponse
import net.polix.system.integration.IntegrationType
import net.polix.system.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *
 * AUTHOR: ScarDay
 * CREATED AT: 06/11/2023 | 19:22
 *
 **/

@RestController
@RequestMapping("/api")
class InfoUserController {


    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var localizationService: LocalizationService

    @GetMapping("/user.get")
    fun getinfo(@RequestParam("user_id") userId: Long?): ResponseEntity<Any> {

        if (userId == null) {
            val error = StatusInfoResponse(403, "Missing parameter: user_id")
            return ResponseEntity(
                error,
                HttpStatus.FORBIDDEN
            )
        }

        if (!userService.repository.existsById(userId)) {
            val model = StatusInfoResponse(404, "User not found")
            return ResponseEntity(
                model,
                HttpStatus.NOT_FOUND
            )
        }

        val user = userService.createUser(userId, IntegrationType.VK)

        val model = UserInfoResponse(StatusInfoResponse(200, "Success"), user.permissions, localizationService.findMessage(user.lang, user.lang.key).toString(), GroupInfo(user.status.id, user.status.displayName, user.status.priority, user.status.permissions))
        return ResponseEntity(model, HttpStatus.OK)
    }

}
