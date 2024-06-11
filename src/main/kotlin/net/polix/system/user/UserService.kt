package net.polix.system.user

import net.polix.system.integration.IntegrationType
import org.springframework.stereotype.Service

@Service
class UserService(
    val repository: UserRepository
) {


    fun createUser(id: Long, type: IntegrationType): User {
        if (repository.existsByIdAndType(id, type)) {
            return repository.findByIdAndType(id, type).get()
        }

        return User(id, type)
    }

}